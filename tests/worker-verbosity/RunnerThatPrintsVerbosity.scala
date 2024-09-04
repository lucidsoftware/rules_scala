package anx.cancellation

import higherkindness.rules_scala.common.worker.WorkerMain
import higherkindness.rules_scala.common.sandbox.SandboxUtil

import java.io.{InputStream, PrintStream}
import java.nio.file.{Files, Path, Paths}

object RunnerThatPrintsVerbosity extends WorkerMain[Unit] {
  override def init(args: Option[Array[String]]): Unit = ()
  override def work(ctx: Unit, args: Array[String], out: PrintStream, workDir: Path, verbosity: Int): Unit = {
      out.println(s"Verbosity: ${verbosity}")
      Files.createFile(SandboxUtil.getSandboxPath(workDir, Paths.get(args(0))))
  }
}
