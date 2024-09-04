package anx.cancellation

import higherkindness.rules_scala.common.worker.WorkerMain
import higherkindness.rules_scala.common.sandbox.SandboxUtil

import java.io.{InputStream, PrintStream}
import java.nio.file.{Files, Path, Paths}

class RunnerForCancelSpec(stdin: InputStream, stdout: PrintStream)
  extends WorkerMain[Unit](stdin, stdout) {

  override def init(args: Option[Array[String]]): Unit = ()

  override def work(ctx: Unit, args: Array[String], out: PrintStream, workDir: Path, verbosity: Int): Unit = {
      var interrupted = false
      var i = 0

      while (i < 7 && !interrupted) {
        Thread.sleep(1000)
        if (Thread.interrupted()) {
          interrupted = true
        }

        i += 1
      }
  }
}
