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

class CancelSpec extends AnyFlatSpec {
  def withRunningWorker[S](
    fn: (OutputStream, InputStream) => S
  ): S = {
    WorkerTestUtil.withIOStreams { (testOut, testIn, workerStdOut, workerStdIn) =>
      val worker = new RunnerForCancelSpec(workerStdIn, workerStdOut)
      Future(worker.main(Array("--persistent_worker")))(ExecutionContext.global)

      fn(testOut, testIn)
    }
  }

  "Annex workers" should "cancel work requests when they receive a cancel request" in {
    val requestId = 1
    val workRequest = WorkerTestUtil.getWorkRequest(requestId)
    val cancelRequest = WorkerTestUtil.getCancelRequest(requestId)

    withRunningWorker { (testOut, testIn) =>
      workRequest.writeDelimitedTo(testOut)
      cancelRequest.writeDelimitedTo(testOut)

      val response = WorkerProtocol.WorkResponse.parseDelimitedFrom(testIn)
      assert(response.getRequestId() == requestId)
      assert(response.getWasCancelled() == true)
    }
  }

  it should "cancel work requests when they receive a cancel request for a running work request" in {
    val requestId = 1
    val workRequest = WorkerTestUtil.getWorkRequest(requestId)
    val cancelRequest = WorkerTestUtil.getCancelRequest(requestId)

    withRunningWorker { (testOut, testIn) =>
      workRequest.writeDelimitedTo(testOut)
      // The worker works by sleeping for 7 seconds, so while 2 seconds isn't a guarantee things are
      // running, it's a pretty good bet that they will be.
      Thread.sleep(2000)
      cancelRequest.writeDelimitedTo(testOut)

      val response = WorkerProtocol.WorkResponse.parseDelimitedFrom(testIn)
      assert(response.getRequestId() == requestId)
      assert(response.getWasCancelled() == true)
    }
  }

  it should "handle duplicate cancellation requests without responding to them" in {
    val requestId = 1
    val workRequest = WorkerTestUtil.getWorkRequest(requestId)
    val cancelRequest = WorkerTestUtil.getCancelRequest(requestId)

    withRunningWorker { (testOut, testIn) =>
      workRequest.writeDelimitedTo(testOut)
      cancelRequest.writeDelimitedTo(testOut)

      val response = WorkerProtocol.WorkResponse.parseDelimitedFrom(testIn)
      assert(response.getRequestId() == requestId)
      assert(response.getWasCancelled() == true)

      cancelRequest.writeDelimitedTo(testOut)

      assertThrows[TimeoutException] {
        val response2 = Future {
          WorkerProtocol.WorkResponse.parseDelimitedFrom(testIn)
        }(scala.concurrent.ExecutionContext.global)

        Await.result(response2, 1.seconds)
      }
    }
  }

  it should "handle cancellation requests for requests that don't exist without responding to them" in {
    val requestId = 1
    val cancelRequest = WorkerTestUtil.getCancelRequest(requestId)

    withRunningWorker { (testOut, testIn) =>
      cancelRequest.writeDelimitedTo(testOut)

      assertThrows[TimeoutException] {
        val response = Future {
          WorkerProtocol.WorkResponse.parseDelimitedFrom(testIn)
        }(scala.concurrent.ExecutionContext.global)

        Await.result(response, 1.seconds)
      }
    }
  }

  it should "handle cancellation requests for requests that already completed" in {
    val requestId = 1
    val workRequest = WorkerTestUtil.getWorkRequest(requestId)
    val cancelRequest = WorkerTestUtil.getCancelRequest(requestId)

    withRunningWorker { (testOut, testIn) =>
      workRequest.writeDelimitedTo(testOut)

      val response = WorkerProtocol.WorkResponse.parseDelimitedFrom(testIn)
      assert(response.getRequestId() == requestId)
      assert(response.getWasCancelled() == false)

      cancelRequest.writeDelimitedTo(testOut)

      assertThrows[TimeoutException] {
        val response2 = Future {
          WorkerProtocol.WorkResponse.parseDelimitedFrom(testIn)
        }(scala.concurrent.ExecutionContext.global)

        Await.result(response2, 1.seconds)
      }
    }
  }

  it should "error when receiving multiple work requests for the same request id" in {
    val requestId = 1
    val workRequest = WorkerTestUtil.getWorkRequest(requestId)

    WorkerTestUtil.withIOStreams { (testOut, testIn, workerStdOut, workerStdIn) =>
      val worker = new RunnerForCancelSpec(workerStdIn, workerStdOut)
      val server = Future(worker.main(Array("--persistent_worker")))(ExecutionContext.global)

      workRequest.writeDelimitedTo(testOut)
      workRequest.writeDelimitedTo(testOut)

      // The worker currently throws an error when it receives a duplicate request
      assertThrows[AnnexDuplicateActiveRequestException] {
        Await.result(server, 5.second)
      }
    }
  }
}