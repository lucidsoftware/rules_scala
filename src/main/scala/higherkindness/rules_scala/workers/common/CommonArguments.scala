package higherkindness.rules_scala
package workers.common

import common.args.ArgsUtil.PathArgumentType
import common.args.implicits._
import common.sandbox.SandboxUtil
import net.sourceforge.argparse4j.impl.{Arguments => ArgumentsImpl}
import net.sourceforge.argparse4j.inf.{Argument, ArgumentParser, ArgumentType, Namespace}
import java.util.{Collections, List as JList}
import scala.collection.mutable.Buffer
import scala.jdk.CollectionConverters.*
import java.io.File
import java.nio.file.{Path, Paths}

class CommonArguments private (
  val analyses: List[Analysis],
  val compilerBridge: Path,
  val compilerClasspath: List[Path],
  val compilerOptions: List[String],
  val classpath: List[Path],
  val debug: Boolean,
  val javaCompilerOptions: List[String],
  val label: String,
  val logLevel: LogLevel,
  val mainManifest: Path,
  val outputAnalysisStore: Path,
  val outputJar: Path,
  val outputUsed: Path,
  val plugins: List[Path],
  val sourceJars: List[Path],
  val tmpDir: Path,
  val sources: List[Path],
)

class Analysis private (
  val label: String,
  val analysisStore: Path,
  val jars: List[Path],
)

object Analysis {
  def apply(workDir: Path, label: String, analysisStore: String, jars: List[String]): Analysis = {
    new Analysis(
      label,
      SandboxUtil.getSandboxPath(workDir, Paths.get(analysisStore)),
      jars.map(jar => SandboxUtil.getSandboxPath(workDir, Paths.get(jar))),
    )
  }
}

object CommonArguments {
  private val scala2SemanticDbTargetRootRegex = """-P:semanticdb:targetroot:(.*)""".r
  private val scala3SemanticDbTargetRootRegex = """-semanticdb-target:(.*)""".r

  private def adjustCompilerOptions(workDir: Path, options: List[String]) = {
    def adjustStringPath(path: String) =
      SandboxUtil.getSandboxPath(workDir, Paths.get(path)).toString

    options.flatMap {
      case scala2SemanticDbTargetRootRegex(path) =>
        List(s"-P:semanticdb:sourceroot:${workDir.toString}", s"-P:semanticdb:targetroot:${adjustStringPath(path)}")

      case scala3SemanticDbTargetRootRegex(path) =>
        List(s"-semanticdb-target:${adjustStringPath(path)}", s"-sourceroot:${workDir.toString}")

      case option => List(option)
    }
  }

  /**
   * Adds argument parsers for CommonArguments to the given ArgumentParser and then returns the mutated ArgumentParser.
   */
  def add(parser: ArgumentParser): ArgumentParser = {
    parser
      .addArgument("--analysis")
      .action(ArgumentsImpl.append)
      .help("Analysis, given as: _label analysis_store [jar ...]")
      .metavar("args")
      .nargs("*")
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
      .action(ArgumentsImpl.append)
      .metavar("option")
    parser
      .addArgument("--classpath")
      .help("Compilation classpath")
      .metavar("path")
      .nargs("*")
      .`type`(PathArgumentType.apply())
      .setDefault_(Collections.emptyList)
    parser
      .addArgument("--debug")
      .metavar("debug")
      .`type`(ArgumentsImpl.booleanType)
      .setDefault_(false)
    parser
      .addArgument("--java_compiler_option")
      .help("Java compiler option")
      .action(ArgumentsImpl.append)
      .metavar("option")
    parser
      .addArgument("--label")
      .help("Bazel label")
      .metavar("label")
    parser
      .addArgument("--log_level")
      .help("Log level")
      .choices(LogLevel.values.keys.toSeq: _*)
      .setDefault_(LogLevel.Warn.level)
    parser
      .addArgument("--main_manifest")
      .help("List of main entry points")
      .metavar("file")
      .required(true)
      .`type`(PathArgumentType.apply())
    parser
      .addArgument("--output_analysis_store")
      .help("Output Analysis Store")
      .metavar("path")
      .required(true)
      .`type`(PathArgumentType.apply())
    parser
      .addArgument("--output_jar")
      .help("Output jar")
      .metavar("path")
      .required(true)
      .`type`(PathArgumentType.apply())
    parser
      .addArgument("--output_used")
      .help("Output list of used jars")
      .metavar("path")
      .required(true)
      .`type`(PathArgumentType.apply())
    parser
      .addArgument("--plugins")
      .help("Compiler plugins")
      .metavar("path")
      .nargs("*")
      .`type`(PathArgumentType.apply())
      .setDefault_(Collections.emptyList)
    parser
      .addArgument("--source_jars")
      .help("Source jars")
      .metavar("path")
      .nargs("*")
      .`type`(PathArgumentType.apply())
      .setDefault_(Collections.emptyList)
    parser
      .addArgument("--tmp")
      .help("Temporary directory")
      .metavar("path")
      .required(true)
      .`type`(PathArgumentType.apply())
    parser
      .addArgument("sources")
      .help("Source files")
      .metavar("source")
      .nargs("*")
      .`type`(PathArgumentType.apply())
      .setDefault_(Collections.emptyList)

    parser
  }

  def apply(namespace: Namespace, workDir: Path): CommonArguments = {
    val analysisArgs = Option(namespace.getList[JList[String]]("analysis")).map(_.asScala).getOrElse(List.empty)

    val analyses: List[Analysis] = analysisArgs.view
      .map(_.asScala)
      .map { analysisArg =>
        // Analysis strings are of the format: _label analysis_store [jar ...]
        val label = analysisArg(0)
        val analysisStore = analysisArg(1)
        val jars = analysisArg.drop(2).toList
        // Drop the leading _ on the label, which was added to avoid triggering argparse's arg file detection
        Analysis(workDir, label.tail, analysisStore, jars)
      }
      .toList

    new CommonArguments(
      analyses = analyses,
      compilerBridge = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("compiler_bridge")),
      compilerClasspath = SandboxUtil.getSandboxPaths(workDir, namespace.getList[Path]("compiler_classpath")),
      compilerOptions = adjustCompilerOptions(
        workDir,
        Option(namespace.getList[String]("compiler_option")).map(_.asScala.toList).getOrElse(List.empty),
      ),
      classpath = SandboxUtil.getSandboxPaths(workDir, namespace.getList[Path]("classpath")),
      debug = namespace.getBoolean("debug"),
      javaCompilerOptions = namespace.getList[String]("java_compiler_option").asScala.toList,
      label = namespace.getString("label"),
      logLevel = LogLevel(namespace.getString("log_level")),
      mainManifest = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("main_manifest")),
      outputAnalysisStore = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("output_analysis_store")),
      outputJar = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("output_jar")),
      outputUsed = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("output_used")),
      plugins = SandboxUtil.getSandboxPaths(workDir, namespace.getList[Path]("plugins")),
      sourceJars = SandboxUtil.getSandboxPaths(workDir, namespace.getList[Path]("source_jars")),
      tmpDir = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("tmp")),
      sources = SandboxUtil.getSandboxPaths(workDir, namespace.getList[Path]("sources")),
    )
  }

}
