package higherkindness.rules_scala
package common.worker

import common.error.{AnnexDuplicateActiveRequestException, AnnexWorkerError}
import com.google.devtools.build.lib.worker.WorkerProtocol
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream, PrintStream}
import java.nio.file.{Path, Paths}
import java.util.concurrent.{Callable, CancellationException, ConcurrentHashMap, ForkJoinPool, FutureTask}
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, ExecutionException, Future}
import scala.util.{Failure, Success, Using}

abstract class WorkerMain[S](stdin: InputStream = System.in, stdout: PrintStream = System.out) {

  protected[this] def init(args: Option[Array[String]]): S

  protected[this] def work(ctx: S, args: Array[String], out: PrintStream, workDir: Path): Unit

  protected[this] var isWorker = false

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
        val activeRequests = new ConcurrentHashMap[Int, CancellableTask[Int]](poolSize)

        def writeResponse(
          requestId: Int,
          maybeOutStream: Option[OutputStream],
          maybeExitCode: Option[Int],
          wasCancelled: Boolean = false,
        ): Unit = {
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

          activeRequests.remove(requestId)
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

          // If this is a cancel request, we need to cancel a previously sent WorkRequest
          // Arguments and inputs fields on cancel requests "must be empty and should be ignored"
          if (request.getCancel()) {
            System.err.println(s"Cancellation WorkRequest received for request id: $requestId")

            // From the Bazel doc: "The server may send cancel requests for requests that the worker
            // has already responded to, in which case the cancel request must be ignored."
            Option(activeRequests.get(requestId)).foreach { activeRequest =>
              // Cancel will wait for the thread to complete or be interrupted, so we do it in a future
              // to prevent blocking the worker from processing more requests
              Future(activeRequest.cancel(mayInterruptIfRunning = true))(scala.concurrent.ExecutionContext.global)
            }
          } else {
            val args = request.getArgumentsList.toArray(Array.empty[String])
            val sandboxDir = Path.of(request.getSandboxDir())
            System.err.println(s"WorkRequest $requestId received with args: ${request.getArgumentsList}")

            // We go through this hullabaloo with output streams being defined out here, so we can
            // close them after the async work in the Future is all done.
            // If we do something synchronous with Using, then there's a race condition where the
            // streams can get closed before the Future is completed.
            var outStream: ByteArrayOutputStream = null
            var out: PrintStream = null

            val workTask = CancellableTask {
              outStream = new ByteArrayOutputStream
              out = new PrintStream(outStream)
              try {
                work(ctx, args, out, sandboxDir)
                0
              } catch {
                case e @ AnnexWorkerError(code, _, _) =>
                  e.print(out)
                  code
              }
            }

            workTask.future
              .andThen {
                // Work task succeeded or failed in an expected way
                case Success(code) =>
                  out.flush()
                  writeResponse(requestId, Some(outStream), Some(code))
                  System.err.println(s"WorkResponse $requestId sent with code $code")

                case Failure(e: ExecutionException) =>
                  e.getCause() match {
                    // Task successfully cancelled
                    case cancelError: InterruptedException =>
                      writeResponse(requestId, None, None, wasCancelled = true)
                      System.err.println(
                        s"Cancellation WorkResponse sent for request id: $requestId in response to an" +
                          " InterruptedException",
                      )

                    // Work task threw a non-fatal error
                    case e =>
                      e.printStackTrace(out)
                      out.flush()
                      writeResponse(requestId, Some(outStream), Some(-1))
                      System.err.println(
                        "Encountered an uncaught exception that was wrapped in an ExecutionException while" +
                          s" proccessing the Future for WorkRequest $requestId. This usually means a non-fatal" +
                          " error was thrown in the Future.",
                      )
                      e.printStackTrace(System.err)
                  }

                // Task successfully cancelled
                case Failure(e: CancellationException) =>
                  writeResponse(requestId, None, None, wasCancelled = true)
                  System.err.println(
                    s"Cancellation WorkResponse sent for request id: $requestId in response to a" +
                      " CancellationException",
                  )

                // Work task threw an uncaught exception
                case Failure(e) =>
                  e.printStackTrace(out)
                  out.flush()
                  writeResponse(requestId, Some(outStream), Some(-1))
                  System.err.println(s"Uncaught exception in Future while proccessing WorkRequest $requestId:")
                  e.printStackTrace(System.err)
              }(scala.concurrent.ExecutionContext.global)
              .andThen { case _ =>
                out.close()
                outStream.close()
              }(scala.concurrent.ExecutionContext.global)

            // putIfAbsent will return a non-null value if there was already a value in the map
            // for this requestId. If that's the case, we have a book keeping error or there are
            // two active requests with the same ID. Either of which is not good and something we
            // should just crash on.
            if (activeRequests.putIfAbsent(requestId, workTask) != null) {
              throw new AnnexDuplicateActiveRequestException("Received a WorkRequest with an already active requestId.")
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
        Using.Manager { use =>
          val outStream = use(new ByteArrayOutputStream())
          val out = use(new PrintStream(outStream))
          try {
            work(
              init(args = None),
              args.toArray,
              out,
              workDir = Path.of(""),
            )
          } catch {
            // This error means the work function encountered an error that we want to not be caught
            // inside that function. That way it stops work and exits the function. However, we
            // also don't want to crash the whole program.
            case e: AnnexWorkerError => e.print(out)
          } finally {
            out.flush()
          }

          outStream.writeTo(System.err)
        }.get
    }
  }
}
