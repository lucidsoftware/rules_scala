package higherkindness.rules_scala
package workers.common

import common.args.ArgsUtil.PathArgumentType
import common.args.implicits.*
import common.sandbox.SandboxUtil
import net.sourceforge.argparse4j.impl.Arguments as ArgumentsImpl
import net.sourceforge.argparse4j.inf.{Argument, ArgumentParser, ArgumentType, Namespace}
import java.util.{Collections, List as JList}
import scala.annotation.nowarn
import scala.collection.mutable.Buffer
import scala.jdk.CollectionConverters.*
import java.io.File
import java.nio.file.{Path, Paths}

class CommonArguments private (
  val analyses: List[Analysis],
  val compilerBridge: Path,
  val compilerClasspath: List[Path],
  val compilerOptions: Array[String],

  /**
   * With [[https://bazel.build/remote/multiplex#multiplex_sandboxing multiplex sandboxing]], Bazel generates a separate
   * sandbox directory for each worker invocation, which means the paths to build artifacts won't be known until the
   * execution phase. However, compiler options are usually generated during the analysis phase.
   *
   * Some compiler options (currently, only those regarding SemanticDB) reference paths to build artifacts and need to
   * be adjusted to be relative to the sandbox directory. It's more performant for those options to be passed in a
   * separate list so we don't have to scan through every compiler option and potentially modify it. In our experience,
   * this resulted in a ~10% worker speedup.
   */
  val compilerOptionsReferencingPaths: List[String],
  val classpath: List[Path],
  val debug: Boolean,
  val javaCompilerOptions: Array[String],
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
  private def adjustCompilerOptions(workDir: Path, options: List[String]) = options.map { option =>
    val i = option.lastIndexOf(' ')
    val withPathReplaced = if (i == -1) {
      option
    } else {
      val template = option.slice(0, i)
      val path = option.slice(i + 1, option.length)

      template.replace(
        "${path}",
        SandboxUtil.getSandboxPath(workDir, Paths.get(path)).toString,
      )
    }

    // Use an absolute path here for the work dir to avoid problems when the working directory is " "
    withPathReplaced
      .replace("${workDir}", workDir.toAbsolutePath().normalize().toString())
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
      .addArgument("--compiler_option_referencing_path")
      .help("Compiler option referencing the paths to build artifact(s)")
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
      compilerOptions = Option(namespace.getList[String]("compiler_option"))
        .map(_.asScala.toArray)
        .getOrElse(Array.empty),
      compilerOptionsReferencingPaths = adjustCompilerOptions(
        workDir,
        Option(namespace.getList[String]("compiler_option_referencing_path"))
          .map(_.asScala.toList)
          .getOrElse(List.empty),
      ),
      classpath = SandboxUtil.getSandboxPaths(workDir, namespace.getList[Path]("classpath")),
      debug = namespace.getBoolean("debug"),
      javaCompilerOptions = namespace.getList[String]("java_compiler_option").asScala.toArray,
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
