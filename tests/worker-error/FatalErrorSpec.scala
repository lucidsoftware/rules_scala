package anx.cancellation

import anx.testlib.worker.WorkerTestUtil
import higherkindness.rules_scala.common.error.AnnexDuplicateActiveRequestException
import org.scalatest.flatspec.AnyFlatSpec
import com.google.devtools.build.lib.worker.WorkerProtocol
import java.io.{InputStream, OutputStream, PipedInputStream, PipedOutputStream, PrintStream}
import java.util.concurrent.TimeoutException
import scala.jdk.CollectionConverters._
import scala.util.Using
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class FatalErrorSpec extends AnyFlatSpec {

  // This test will crash because it calls System.exit. We rely on that happening and then
  // checking in the test shell script that it happened.
  // If anyone knows of a good way to mock System.exit that doesn't rely on changing the SecurityManager
  // (which is deprecated and has been for a while), then we can just do this as a regular test.
  "Annex workers" should "handle when the worker work function throws a fatal error" in {
    WorkerTestUtil.withIOStreams { (testOut, testIn, workerStdOut, workerStdIn) =>
      val worker = new RunnerThatThrowsFatalError(workerStdIn, workerStdOut)
      val server = Future(worker.main(Array("--persistent_worker")))(ExecutionContext.global)

      val requestId = 1
      val workRequest = WorkerTestUtil.getWorkRequest(requestId)

      // Server will exit shortly after this point
      workRequest.writeDelimitedTo(testOut)
      val response = WorkerProtocol.WorkResponse.parseDelimitedFrom(testIn)
      Await.result(server, 100.millisecond)
    }
  }
}