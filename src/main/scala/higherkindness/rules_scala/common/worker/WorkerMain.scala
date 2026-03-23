package higherkindness.rules_scala.common.worker

import higherkindness.rules_scala.common.error.{AnnexDuplicateActiveRequestException, AnnexWorkerError}
import com.google.devtools.build.lib.worker.WorkerProtocol
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream, PrintStream}
import java.nio.channels.ClosedByInterruptException
import java.nio.file.Path
import java.util.concurrent.{CancellationException, ConcurrentHashMap, ForkJoinPool}
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, ExecutionException, Future}
import scala.util.{Failure, Success, Using}
import java.time.{ZoneId, ZonedDateTime}

abstract class WorkerMain[S](stdin: InputStream = System.in, stdout: PrintStream = System.out) {

  protected def init(args: Option[Array[String]]): S

  /**
   * isCancelled is used to determine whether the FutureTask that is executing this work request has been cancelled. If
   * it is safe to Thread.interrupt a process, then that is done and can be checked. Not all workers can be
   * Thread.interrupted safely.
   *
   * TODO(James): document the rest of this function
   */
  protected def work(workRequest: WorkTask[S]): Unit

  /**
   * Indicates whether this program is being executed as a worker or as a regular process. It is a var because we won't
   * know until runtime which one it is.
   */
  protected var isWorker = false

  /**
   * Used to determine whether to interrupt the FutureTasks being executed by this worker using Thread.interrupt or not.
   * It's safe to interrupt many things with Thread.interrupt, but not all things.
   */
  protected val mayInterruptWorkerTasks = true

  final def main(args: Array[String]): Unit = {
    args.toList match {
      case "--persistent_worker" :: args =>
        isWorker = true
        val exceptionHandler = new Thread.UncaughtExceptionHandler {
          override def uncaughtException(t: Thread, err: Throwable): Unit = err match {
            case e: Throwable => {
              // Future catches all NonFatal errors, and wraps them in a Failure, so only Fatal errors get here.
              // If any request thread throws a Fatal error (OOM, StackOverflow, etc.), we can't trust the JVM, so log the error and exit.
              e.printStackTrace(System.err)
              System.exit(1)
            }
          }
        }
        val poolSize = Runtime.getRuntime().availableProcessors()
        val fjp = new ForkJoinPool(
          poolSize,
          ForkJoinPool.defaultForkJoinWorkerThreadFactory,
          exceptionHandler,
          false,
        )
        val ec = ExecutionContext.fromExecutor(fjp)

        // Map of request id to the runnable responsible for executing that request id
        val activeRequests = new ConcurrentHashMap[Int, (WorkerProtocol.WorkRequest, CancellableTask[Int])](poolSize)

        def writeResponse(
          requestId: Int,
          maybeOutStream: Option[OutputStream],
          maybeExitCode: Option[Int],
          wasCancelled: Boolean = false,
        ): Unit = {
          // Remove the request from our book keeping right before we respond to Bazel. If
          // we respond to Bazel about the request before removing it,then there is a race:
          // Bazel could make a request with the same requestId to this worker before the
          // requestId is removed from the worker's book keeping.
          //
          // Ideally Bazel will not send a request to this worker with the same requestId
          // as another request before we've responded to the original request. If that
          // happens, then there's a race regardless of what we do.
          activeRequests.remove(requestId)

          // Defined here so all writes to stdout are synchronized
          stdout.synchronized {
            val builder = WorkerProtocol.WorkResponse.newBuilder
            builder
              .setRequestId(requestId)
              .setWasCancelled(wasCancelled)

            maybeOutStream.foreach { outStream =>
              builder.setOutput(outStream.toString)
            }

            maybeExitCode.foreach { exitCode =>
              builder.setExitCode(exitCode)
            }

            builder
              .build()
              .writeDelimitedTo(stdout)
          }
        }

        /**
         * Think of this function as a single threaded event loop that gets work from Bazle and farms out work to the
         * multi threaded execution context defined above.
         */
        @tailrec
        def process(ctx: S): Unit = {
          val request = WorkerProtocol.WorkRequest.parseDelimitedFrom(stdin)
          if (request == null) {
            return
          }

          val requestId = request.getRequestId()

          // "Passing the --worker_verbose flag to Bazel sets the verbosity field to 10, but smaller
          // or larger values can be used manually for different amounts of output."
          // For more info: https://bazel.build/remote/creating#work-requests
          val verbosity = request.getVerbosity()
          def logVerbose(message: String) = {
            if (verbosity >= 10) {
              val now = ZonedDateTime.now(ZoneId.of("UTC"))
              System.err.println(s"${now}: ${message}")
            }
          }

          // If this is a cancel request, we need to cancel a previously sent WorkRequest
          // Arguments and inputs fields on cancel requests "must be empty and should be ignored"
          if (request.getCancel()) {
            // TODO: Cancellation requests when worker_verbose is set don't set verbosity = 10, so
            // this is unlikely to ever log. See this issue for more info:
            // https://github.com/bazelbuild/bazel/issues/25803
            logVerbose(
              s"Cancellation WorkRequest received for request id: $requestId",
            )

            // From the Bazel doc: "The server may send cancel requests for requests that the worker
            // has already responded to, in which case the cancel request must be ignored."
            Option(activeRequests.get(requestId)).foreach { case (_, workTask) =>
              // Cancel will wait for the thread to complete or be interrupted, so we do it in a future
              // to prevent blocking the worker from processing more requests
              Future(workTask.cancel(mayInterruptIfRunning = mayInterruptWorkerTasks))(
                scala.concurrent.ExecutionContext.global,
              )
            }
          } else {
            val args = request.getArgumentsList.toArray(Array.empty[String])
            val sandboxDir = Path.of(request.getSandboxDir())
            logVerbose(s"WorkRequest received with id: $requestId and args: ${request.getArgumentsList}")

            // We go through this hullabaloo with output streams being defined out here, so we can
            // close them after the async work in the Future is all done.
            // If we do something synchronous with Using, then there's a race condition where the
            // streams can get closed before the Future is completed.
            var maybeOutStream: Option[ByteArrayOutputStream] = None
            var maybeOut: Option[PrintStream] = None

            def flushOut(): Unit = {
              maybeOut.map(_.flush())
            }

            def doWork(isCancelled: Function0[Boolean]) = {
              val outStream = new ByteArrayOutputStream()
              val out = new PrintStream(outStream)
              maybeOutStream = Some(outStream)
              maybeOut = Some(out)
              try {
                work(WorkTask(ctx, args, out, sandboxDir, verbosity, isCancelled))
                0
              } catch {
                case e @ AnnexWorkerError(code, _, _) =>
                  e.print(out)
                  code
              }
            }

            val workTask = CancellableTask(doWork)

            workTask.future
              .andThen {
                // Work task succeeded or failed in an expected way
                case Success(code) =>
                  flushOut()
                  writeResponse(requestId, maybeOutStream, Some(code))
                  logVerbose(s"WorkResponse for request id: $requestId sent with code $code")

                // `CancellableTask` wraps all exceptions in `ExecutionException`, so we need to unwrap them here
                case Failure(e: ExecutionException)
                    if e.getCause().isInstanceOf[CancellationException]
                      || e.getCause().isInstanceOf[ClosedByInterruptException]
                      || e.getCause().isInstanceOf[InterruptedException] =>
                  flushOut()
                  writeResponse(requestId, None, None, wasCancelled = true)
                  logVerbose(
                    s"Cancellation WorkResponse sent for request id: $requestId in response to a ${e.getCause().getClass.getCanonicalName}",
                  )

                // Work task threw an uncaught exception
                case Failure(e: ExecutionException) if e.getCause() != null =>
                  maybeOut.map(e.getCause().printStackTrace(_))
                  flushOut()
                  writeResponse(requestId, maybeOutStream, Some(-1))
                  logVerbose(
                    s"Uncaught exception in Future while proccessing WorkRequest id: $requestId\nType: ${e.getCause().getClass.getCanonicalName}",
                  )
                  e.getCause().printStackTrace(System.err)

                // Task successfully cancelled
                case Failure(
                      e @ (_: CancellationException | _: ClosedByInterruptException | _: InterruptedException),
                    ) =>
                  flushOut()
                  writeResponse(requestId, None, None, wasCancelled = true)
                  logVerbose(
                    s"Cancellation WorkResponse sent for request id: $requestId in response to a " +
                      e.getClass.getCanonicalName,
                  )

                // Work task threw an uncaught exception. This branch should never be activated because of the
                // exception wrapping described above, but it never hurts to be defensive ¯\_(ツ)_/¯
                case Failure(e) =>
                  maybeOut.map(e.printStackTrace(_))
                  flushOut()
                  writeResponse(requestId, maybeOutStream, Some(-1))
                  logVerbose(
                    s"Uncaught exception in Future while proccessing WorkRequest id: $requestId\nType: ${e.getClass.getCanonicalName}",
                  )
                  e.printStackTrace(System.err)
              }(scala.concurrent.ExecutionContext.global)
              .andThen { case _ =>
                maybeOut.map(_.close())
                maybeOutStream.map(_.close())
              }(scala.concurrent.ExecutionContext.global)

            // putIfAbsent will return a non-null value if there was already a value in the map
            // for this requestId. If that's the case, we have a book keeping error or there are
            // two active requests with the same ID. Either of which is not good and something we
            // should just crash on.
            val alreadyActiveRequest = activeRequests.putIfAbsent(requestId, (request, workTask))
            if (alreadyActiveRequest != null) {
              val (activeRequest, _) = alreadyActiveRequest
              throw new AnnexDuplicateActiveRequestException(
                s"""Received a WorkRequest with an already active request id: ${requestId}.
                Currently active request: ${activeRequest.toString}
                New request with the same id: ${request.toString}
                """,
              )
            } else {
              workTask.execute(ec)
            }
          }

          process(ctx)
        }

        Using.resource(new ByteArrayInputStream(Array.emptyByteArray)) { inStream =>
          try {
            System.setIn(inStream)
            System.setOut(System.err)
            process(init(Some(args.toArray)))
          } finally {
            System.setIn(stdin)
            System.setOut(stdout)
          }
        }

      case args =>
        val returnCode = Using.Manager { use =>
          val outStream = use(new ByteArrayOutputStream())
          val out = use(new PrintStream(outStream))
          val returnCode =
            try {
              work(
                WorkTask(
                  init(args = None),
                  args.toArray,
                  out,
                  workDir = Path.of(""),
                  verbosity = 0,
                  isCancelled = () => false,
                ),
              )

              0
            } catch {
              // This error means the work function encountered an error that we want to not be caught
              // inside that function. That way it stops work and exits the function. However, we
              // also don't want to crash the whole program.
              case e: AnnexWorkerError =>
                e.print(out)
                e.code
            } finally {
              out.flush()
            }

          outStream.writeTo(System.err)

          returnCode
        }.get

        sys.exit(returnCode)
    }
  }
}
