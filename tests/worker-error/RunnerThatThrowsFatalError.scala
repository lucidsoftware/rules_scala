package anx.cancellation

import higherkindness.rules_scala.common.worker.{WorkerMain, WorkTask}

import java.io.{InputStream, PrintStream}

class RunnerThatThrowsFatalError(stdin: InputStream, stdout: PrintStream)
  extends WorkerMain[Unit](stdin, stdout) {

  override def init(args: Option[Array[String]]): Unit = ()

  override def work(task: WorkTask[Unit]): Unit = {
    throw new OutOfMemoryError()
  }
}
