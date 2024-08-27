package anx.cancellation

import anx.testlib.worker.WorkerTestUtil
import org.scalatest.flatspec.AnyFlatSpec
import com.google.devtools.build.lib.worker.WorkerProtocol
import java.io.{InputStream, OutputStream, PipedInputStream, PipedOutputStream, PrintStream}
import scala.util.Using
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class ErrorSpec extends AnyFlatSpec {
  "Annex workers" should "handle when the worker work function throws an exception" in {
    WorkerTestUtil.withIOStreams { (testOut, testIn, workerStdOut, workerStdIn) =>
      val worker = new RunnerThatThrowsException(workerStdIn, workerStdOut)
      val server = Future(worker.main(Array("--persistent_worker")))(ExecutionContext.global)

      val requestId = 1
      val workRequest = WorkerTestUtil.getWorkRequest(requestId)

      workRequest.writeDelimitedTo(testOut)

      val response = WorkerProtocol.WorkResponse.parseDelimitedFrom(testIn)
      assert(response.getRequestId() == requestId)
      assert(response.getWasCancelled() == false)
      assert(response.getExitCode() == -1)
    }
  }

  it should "handle when the worker work function throws an error" in {
    WorkerTestUtil.withIOStreams { (testOut, testIn, workerStdOut, workerStdIn) =>
      val worker = new RunnerThatThrowsError(workerStdIn, workerStdOut)
      val server = Future(worker.main(Array("--persistent_worker")))(ExecutionContext.global)

      val requestId = 1
      val workRequest = WorkerTestUtil.getWorkRequest(requestId)

      workRequest.writeDelimitedTo(testOut)

      val response = WorkerProtocol.WorkResponse.parseDelimitedFrom(testIn)
      assert(response.getRequestId() == requestId)
      assert(response.getWasCancelled() == false)
      assert(response.getExitCode() == -1)
    }
  }
}