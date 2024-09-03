package anx.testlib.worker

import com.google.devtools.build.lib.worker.WorkerProtocol
import java.io.{InputStream, OutputStream, PipedInputStream, PipedOutputStream, PrintStream}
import scala.jdk.CollectionConverters._
import scala.util.Using

object WorkerTestUtil {
  def getWorkRequest(requestId: Int): WorkerProtocol.WorkRequest = {
    WorkerProtocol.WorkRequest.newBuilder
      .addAllArguments(Iterable.empty[String].asJava)
      .setRequestId(requestId)
      .setCancel(false)
      .setVerbosity(0)
      .setSandboxDir("")
      .build()
  }

  def getCancelRequest(requestId: Int): WorkerProtocol.WorkRequest = {
    WorkerProtocol.WorkRequest.newBuilder
      .setRequestId(requestId)
      .setCancel(true)
      .setVerbosity(0)
      .setSandboxDir("")
      .build()
  }

  def withIOStreams[S](
    fn: (OutputStream, InputStream, PrintStream, InputStream) => S
  ): S = Using.Manager { use =>
    val testOut = use(new PipedOutputStream())
    val workerStdIn = use(new PipedInputStream(testOut))

    val workerOut = use(new PipedOutputStream())
    val workerStdOut = use(new PrintStream(workerOut))
    val testIn = use(new PipedInputStream(workerOut))

    fn(testOut, testIn, workerStdOut, workerStdIn)
  }.get
}