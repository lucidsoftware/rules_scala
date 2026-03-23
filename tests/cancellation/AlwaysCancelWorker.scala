package anx.cancellation

import higherkindness.rules_scala.common.worker.{WorkerMain, WorkTask}

import java.io.{InputStream, PrintStream}

/**
 * A worker that immediately throws an InterruptedException. This simulates what happens when a worker calls
 * [[higherkindness.rules_scala.common.interrupt.InterruptUtil.throwIfInterrupted]] and finds the thread has been
 * interrupted.
 */
class AlwaysCancelWorker(stdin: InputStream, stdout: PrintStream)
  extends WorkerMain[Unit](stdin, stdout) {
  override def init(args: Option[Array[String]]): Unit = ()
  override def work(task: WorkTask[Unit]): Unit = {
    throw new InterruptedException("WorkRequest was cancelled via Thread interruption.")
  }
}
