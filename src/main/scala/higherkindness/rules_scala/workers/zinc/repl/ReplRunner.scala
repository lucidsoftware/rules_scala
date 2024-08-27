package higherkindness.rules_scala
package workers.zinc.repl

import common.args.ArgsUtil.PathArgumentType
import common.args.implicits._
import common.sandbox.SandboxUtil
import workers.common.LogLevel
import workers.common.AnnexLogger
import workers.common.AnnexScalaInstance
import workers.common.FileUtil
import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.util.Collections
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.{ArgumentParser, Namespace}
import sbt.internal.inc.{PlainVirtualFile, PlainVirtualFileConverter, ZincUtil}
import scala.jdk.CollectionConverters._
import xsbti.Logger

object ReplRunner {

  private[this] class ReplArgs private (
    val logLevel: LogLevel,
  )

  private[this] object ReplArgs {
    def apply(namespace: Namespace): ReplArgs = {
      new ReplArgs(
        logLevel = LogLevel(namespace.getString("log_level")),
      )
    }
  }

  private[this] val argParser = {
    val parser = ArgumentParsers.newFor("repl").addHelp(true).defaultFormatWidth(80).fromFilePrefix("@").build()
    parser
      .addArgument("--log_level")
      .help("Log level")
      .choices(LogLevel.values.keys.toSeq: _*)
      .setDefault_(LogLevel.Warn.level)
    parser
  }

  private[this] class ReplRequest private (
    val classpath: List[Path],
    val compilerBridge: Path,
    val compilerClasspath: List[Path],
    val compilerOptions: List[String],
  )

  private[this] object ReplRequest {
    def apply(runPath: Path, namespace: Namespace): ReplRequest = {
      new ReplRequest(
        classpath = SandboxUtil.getSandboxPaths(runPath, namespace.getList[Path]("classpath")),
        compilerBridge = SandboxUtil.getSandboxPath(runPath, namespace.get[Path]("compiler_bridge")),
        compilerClasspath = SandboxUtil.getSandboxPaths(runPath, namespace.getList[Path]("compiler_classpath")),
        compilerOptions =
          Option(namespace.getList[String]("compiler_option")).map(_.asScala.toList).getOrElse(List.empty),
      )
    }
  }

  private[this] val replArgParser = {
    val parser = ArgumentParsers.newFor("repl-args").addHelp(true).defaultFormatWidth(80).fromFilePrefix("@").build()
    parser
      .addArgument("--classpath")
      .help("Compilation classpath")
      .metavar("path")
      .nargs("*")
      .`type`(PathArgumentType.apply())
      .setDefault_(Collections.emptyList)
    parser
      .addArgument("--compiler_bridge")
      .help("Compiler bridge")
      .metavar("path")
      .required(true)
      .`type`(PathArgumentType.apply())
    parser
      .addArgument("--compiler_classpath")
      .help("Compiler classpath")
      .metavar("path")
      .nargs("*")
      .`type`(PathArgumentType.apply())
      .setDefault_(Collections.emptyList)
    parser
      .addArgument("--compiler_option")
      .help("Compiler option")
      .action(Arguments.append)
      .metavar("option")
    parser
  }

  def main(args: Array[String]): Unit = {
    // This worker doesn't support multiplex sandboxing, so it won't need to worry about
    // the sandbox directory. This is just here so we don't have to repeat Paths.get everywhere.
    val workDir = Paths.get("")

    val replArgs = ReplArgs(argParser.parseArgsOrFail(args))
    val logger = new AnnexLogger(replArgs.logLevel, workDir, System.err)

    val replArgFile = Paths.get(sys.props("scalaAnnex.test.args"))
    val replRequest = ReplRequest(
      runPath = Paths.get(sys.props("bazel.runPath")),
      replArgParser.parseArgsOrFail(Files.readAllLines(replArgFile).asScala.toArray),
    )

    val scalaInstance = AnnexScalaInstance.getAnnexScalaInstance(
      replRequest.compilerClasspath.map(_.toFile).toArray,
      workDir,
      isWorker = false,
    )

    val scalaCompiler = ZincUtil.scalaCompiler(scalaInstance, replRequest.compilerBridge.toFile)

    scalaCompiler.console(
      (replRequest.compilerClasspath ++ replRequest.classpath).map(file => PlainVirtualFile(file)),
      PlainVirtualFileConverter.converter,
      replRequest.compilerOptions,
      "",
      "",
      logger,
    )()
  }
}
