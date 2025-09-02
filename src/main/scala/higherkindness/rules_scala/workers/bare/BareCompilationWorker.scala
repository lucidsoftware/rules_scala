package higherkindness.rules_scala.workers.bare

import higherkindness.rules_scala.common.error.AnnexWorkerError
import higherkindness.rules_scala.common.worker.{WorkTask, WorkerMain}
import java.io.{BufferedReader, InputStream, InputStreamReader, PrintWriter, StringWriter}
import scala.tools.nsc.{Global, MainClass}
import scala.tools.nsc.reporters.ConsoleReporter

object BareCompilationWorker extends WorkerMain[Unit] {
  private val emptyReader = new BufferedReader(new InputStreamReader(InputStream.nullInputStream()))

  override protected def init(arguments: Option[Array[String]]): Unit = ()
  override protected def work(workRequest: WorkTask[Unit]): Unit = {
    val stringWriter = new StringWriter()
    val printWriter = new PrintWriter(stringWriter)
    val compilerMain = new MainClass {
      override def newCompiler(): Global = {
        val result = super.newCompiler()

        result.reporter = new ConsoleReporter(settings, emptyReader, printWriter)
        result
      }
    }

    if (!compilerMain.process(workRequest.args)) {
      stringWriter.flush()

      throw new AnnexWorkerError(code = 1, message = stringWriter.toString)
    }
  }
}
