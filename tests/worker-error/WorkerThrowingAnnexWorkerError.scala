package anx.cancellation

import higherkindness.rules_scala.common.error.AnnexWorkerError
import higherkindness.rules_scala.common.worker.{WorkerMain, WorkTask}
import java.io.PrintStream
import java.nio.file.Path

object WorkerThrowingAnnexWorkerError extends WorkerMain[Unit] {
  override def init(arguments: Option[Array[String]]): Unit = {}

  override def work(task: WorkTask[Unit]): Unit = {
    throw new AnnexWorkerError(1)
  }
}
