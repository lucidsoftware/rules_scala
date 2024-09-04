package annex.scalafmt

import higherkindness.rules_scala.common.args.ArgsUtil
import higherkindness.rules_scala.common.args.ArgsUtil.PathArgumentType
import higherkindness.rules_scala.common.interrupt.InterruptUtil
import higherkindness.rules_scala.common.sandbox.SandboxUtil
import higherkindness.rules_scala.common.worker.WorkerMain
import higherkindness.rules_scala.workers.common.Color
import java.io.{File, PrintStream}
import java.nio.file.{Files, Path}
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.{ArgumentParser, Namespace}
import org.scalafmt.Scalafmt
import org.scalafmt.config.ScalafmtConfig
import org.scalafmt.sysops.FileOps
import scala.annotation.tailrec
import scala.io.Codec

object ScalafmtRunner extends WorkerMain[Unit] {

  private[this] class ScalafmtRequest private (
    val configFile: Path,
    val inputFile: Path,
    val outputFile: Path,
  )

  private[this] object ScalafmtRequest {
    def apply(workDir: Path, namespace: Namespace): ScalafmtRequest = {
      new ScalafmtRequest(
        configFile = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("config")),
        inputFile = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("input")),
        outputFile = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("output")),
      )
    }
  }

  private[this] val argParser: ArgumentParser = {
    val parser = ArgumentParsers.newFor("scalafmt").addHelp(true).defaultFormatWidth(80).fromFilePrefix("@").build
    parser.addArgument("--config").required(true).`type`(PathArgumentType.apply())
    parser.addArgument("input").`type`(PathArgumentType.apply())
    parser.addArgument("output").`type`(PathArgumentType.apply())
    parser
  }

  protected[this] def init(args: Option[Array[String]]): Unit = {}

  protected[this] def work(worker: Unit, args: Array[String], out: PrintStream, workDir: Path, verbosity: Int): Unit = {
    val workRequest = ScalafmtRequest(workDir, ArgsUtil.parseArgsOrFailSafe(args, argParser, out))
    InterruptUtil.throwIfInterrupted()

    val source = FileOps.readFile(workRequest.inputFile)(Codec.UTF8)

    val config = ScalafmtConfig.fromHoconFile(workRequest.configFile).get
    @tailrec
    def format(code: String): String = {
      InterruptUtil.throwIfInterrupted()
      val formatted = Scalafmt.format(code, config).get
      if (code == formatted) code else format(formatted)
    }

    val output =
      try {
        format(source)
      } catch {
        case e @ (_: org.scalafmt.Error | _: scala.meta.parsers.ParseException) => {
          if (config.runner.fatalWarnings) {
            System.err.println(Color.Error("Exception thrown by Scalafmt and fatalWarnings is enabled"))
            throw e
          } else {
            System.err.println(Color.Warning("Unable to format file due to bug in scalafmt"))
            System.err.println(Color.Warning(e.toString))
            source
          }
        }
      }

    Files.write(workRequest.outputFile, output.getBytes)
    InterruptUtil.throwIfInterrupted()
  }

}
