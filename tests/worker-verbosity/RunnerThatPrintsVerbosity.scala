package anx.cancellation

import higherkindness.rules_scala.common.worker.{WorkerMain, WorkTask}
import higherkindness.rules_scala.common.sandbox.PathResolver

import java.nio.file.{Files, Paths}

object RunnerThatPrintsVerbosity extends WorkerMain[Unit] {
  override def init(args: Option[Array[String]]): Unit = ()

  override def work(task: WorkTask[Unit]): Unit = {
    task.output.println(s"Verbosity: ${task.verbosity}")
    Files.createFile(PathResolver.forPersistentWorker(task.workDir).resolve(Paths.get(task.args(0))))
  }
}
