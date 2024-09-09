package annex.scala.proto

import higherkindness.rules_scala.common.args.ArgsUtil
import higherkindness.rules_scala.common.args.ArgsUtil.PathArgumentType
import higherkindness.rules_scala.common.args.implicits._
import higherkindness.rules_scala.common.interrupt.InterruptUtil
import higherkindness.rules_scala.common.error.AnnexWorkerError
import higherkindness.rules_scala.common.sandbox.SandboxUtil
import higherkindness.rules_scala.common.worker.WorkerMain
import java.io.{File, PrintStream}
import java.nio.file.{Files, Path, Paths}
import java.util.Collections
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.{ArgumentParser, Namespace}
import protocbridge.{ProtocBridge, ProtocRunner}
import scala.jdk.CollectionConverters._
import scalapb.ScalaPbCodeGenerator

object ScalaProtoWorker extends WorkerMain[Unit] {

  private class ScalaProtoRequest private (
    val isGrpc: Boolean,
    val outputDir: Path,
    val protoPaths: List[Path],
    val sources: List[Path],
  )

  private object ScalaProtoRequest {
    def apply(workDir: Path, namespace: Namespace): ScalaProtoRequest = {
      new ScalaProtoRequest(
        isGrpc = namespace.getBoolean("grpc"),
        outputDir = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("output_dir")),
        protoPaths = SandboxUtil.getSandboxPaths(workDir, namespace.getList[Path]("proto_paths")),
        sources = SandboxUtil.getSandboxPaths(workDir, namespace.getList[Path]("sources")),
      )
    }
  }

  private val argParser: ArgumentParser = {
    val parser = ArgumentParsers.newFor("proto").addHelp(true).fromFilePrefix("@").build
    parser
      .addArgument("--output_dir")
      .help("Output dir")
      .metavar("output_dir")
      .`type`(PathArgumentType.apply())
    parser
      .addArgument("--proto_paths")
      .nargs("*")
      .`type`(PathArgumentType.apply())
      .setDefault_(Collections.emptyList)
    parser
      .addArgument("--grpc")
      .action(Arguments.storeTrue)
    parser
      .addArgument("sources")
      .help("Source files")
      .metavar("source")
      .nargs("*")
      .`type`(PathArgumentType.apply())
      .setDefault_(Collections.emptyList)
    parser
  }

  override def init(args: Option[Array[String]]): Unit = ()

  protected def work(ctx: Unit, args: Array[String], out: PrintStream, workDir: Path, verbosity: Int): Unit = {
    val workRequest = ScalaProtoRequest(workDir, ArgsUtil.parseArgsOrFailSafe(args, argParser, out))
    InterruptUtil.throwIfInterrupted()

    val scalaOut = workRequest.outputDir
    Files.createDirectories(scalaOut)
    val outOptions = if (workRequest.isGrpc) {
      "grpc:"
    } else {
      ""
    }
    val params = List(s"--scala_out=${outOptions}${scalaOut}")
      ::: workRequest.protoPaths.map(dir => s"--proto_path=${dir.toString}")
      ::: workRequest.sources.map(_.toString)

    class MyProtocRunner[ExitCode] extends ProtocRunner[Int] {
      def run(args: Seq[String], extraEnv: Seq[(String, String)]): Int = {
        com.github.os72.protocjar.Protoc.runProtoc(args.toArray)
      }
    }

    InterruptUtil.throwIfInterrupted()
    val exitCode = ProtocBridge.runWithGenerators(
      new MyProtocRunner,
      namedGenerators = List("scala" -> ScalaPbCodeGenerator),
      params = params
    )
    if (exitCode != 0) {
      throw new AnnexWorkerError(exitCode)
    }
    InterruptUtil.throwIfInterrupted()
  }

}
