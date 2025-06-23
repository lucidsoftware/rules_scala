package higherkindness.rules_scala.native

import higherkindness.rules_scala.common.error.AnnexWorkerError
import higherkindness.rules_scala.common.worker.{WorkTask, WorkerMain}
import java.io.{BufferedReader, InputStream, InputStreamReader, PrintWriter, StringWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters.*
import scala.tools.nsc.{Global, MainClass}
import scala.tools.nsc.reporters.ConsoleReporter

object CompilerWrapper extends WorkerMain[Unit] {
  private val emptyReader = new BufferedReader(new InputStreamReader(InputStream.nullInputStream()))

  override protected def init(arguments: Option[Array[String]]): Unit = {}

  private def expandArgumentFile(arguments: Array[String]): Array[String] =
    // We assume that the argument file isn't mixed with other arguments
    if (arguments.nonEmpty && arguments(0).startsWith("@")) {
      val argumentFileArgument = arguments(0)
      val argumentFilePath = Paths.get(argumentFileArgument.slice(1, argumentFileArgument.length))

      Files.lines(argumentFilePath, StandardCharsets.UTF_8).iterator.asScala.toArray
    } else {
      arguments
    }

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

      throw new AnnexWorkerError(
        code = 1,
        message = stringWriter.toString,
      )
    }
  }
}
