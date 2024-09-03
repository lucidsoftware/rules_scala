package anx.cancellation

import higherkindness.rules_scala.common.worker.WorkerMain
import higherkindness.rules_scala.common.sandbox.SandboxUtil

import java.io.{InputStream, PrintStream}
import java.nio.file.{Files, Path, Paths}

class RunnerThatThrowsException(stdin: InputStream, stdout: PrintStream)
  extends WorkerMain[Unit](stdin, stdout) {

  override def init(args: Option[Array[String]]): Unit = ()

  override def work(ctx: Unit, args: Array[String], out: PrintStream, workDir: Path): Unit = {
      throw new Exception()
  }
}
