package higherkindness.rules_scala
package common.worker

import common.error.AnnexWorkerError
import com.google.devtools.build.lib.worker.WorkerProtocol
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, OutputStream, PrintStream}
import java.nio.file.Path
import java.util.concurrent.ForkJoinPool
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Using}

trait WorkerMain[S] {

  protected[this] def init(args: Option[Array[String]]): S

  protected[this] def work(ctx: S, args: Array[String], out: PrintStream, workDir: Path): Unit

  protected[this] var isWorker = false

  final def main(args: Array[String]): Unit = {
    args.toList match {
      case "--persistent_worker" :: args =>
        isWorker = true
        val stdin = System.in
        val stdout = System.out
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
        val fjp = new ForkJoinPool(
          Runtime.getRuntime().availableProcessors(),
          ForkJoinPool.defaultForkJoinWorkerThreadFactory,
          exceptionHandler,
          false,
        )
        val ec = ExecutionContext.fromExecutor(fjp)

        def writeResponse(requestId: Int, outStream: OutputStream, code: Int) = {
          // Defined here so all writes to stdout are synchronized
          stdout.synchronized {
            WorkerProtocol.WorkResponse.newBuilder
              .setRequestId(requestId)
              .setOutput(outStream.toString)
              .setExitCode(code)
              .build
              .writeDelimitedTo(stdout)
          }
        }

        @tailrec
        def process(ctx: S): Unit = {
          val request = WorkerProtocol.WorkRequest.parseDelimitedFrom(stdin)
          if (request == null) {
            return
          }
          val args = request.getArgumentsList.toArray(Array.empty[String])
          val sandboxDir = Path.of(request.getSandboxDir())

          // We go through this hullabaloo with output streams being defined out here, so we can
          // close them after the async work in the Future is all done.
          // If we do something synchronous with Using, then there's a race condition where the
          // streams can get closed before the Future is completed.
          var outStream: ByteArrayOutputStream = null
          var out: PrintStream = null

          val requestId = request.getRequestId()
          System.out.println(s"WorkRequest $requestId received with args: ${request.getArgumentsList}")

          val f: Future[Int] = Future {
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
          }(ec)

          f.andThen {
            case Success(code) => {
              out.flush()
              writeResponse(requestId, outStream, code)
              System.out.println(s"WorkResponse $requestId sent with code $code")
            }
            case Failure(e) => {
              e.printStackTrace(out)
              out.flush()
              writeResponse(requestId, outStream, -1)
              System.err.println(s"Uncaught exception in Future while proccessing WorkRequest $requestId:")
              e.printStackTrace(System.err)
            }
          }(scala.concurrent.ExecutionContext.global)
            .andThen { case _ =>
              out.close()
              outStream.close()
            }(scala.concurrent.ExecutionContext.global)
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
          val outStream = use(new ByteArrayOutputStream)
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
