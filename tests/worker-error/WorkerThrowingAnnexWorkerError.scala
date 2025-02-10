package anx.cancellation

import higherkindness.rules_scala.common.error.AnnexWorkerError
import higherkindness.rules_scala.common.worker.WorkerMain
import java.io.PrintStream
import java.nio.file.Path

object WorkerThrowingAnnexWorkerError extends WorkerMain[Unit] {
  override def init(arguments: Option[Array[String]]): Unit = {}
  override def work(
    context: Unit,
    arguments: Array[String],
    output: PrintStream,
    workingDirectory: Path,
    verbosity: Int,
  ): Unit = throw new AnnexWorkerError(1)
}
