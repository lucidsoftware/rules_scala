package higherkindness.rules_scala.workers.zinc.doc

import higherkindness.rules_scala.common.args.ArgsUtil
import higherkindness.rules_scala.common.args.ArgsUtil.PathArgumentType
import higherkindness.rules_scala.common.args.implicits.*
import higherkindness.rules_scala.common.interrupt.InterruptUtil
import higherkindness.rules_scala.common.sandbox.SandboxUtil
import higherkindness.rules_scala.common.worker.{WorkTask, WorkerMain}
import higherkindness.rules_scala.workers.common.{AnnexLogger, AnnexScalaInstance, FileUtil, LogLevel, LoggedReporter}
import java.net.URLClassLoader
import java.nio.file.{Files, NoSuchFileException, Path}
import java.util.Collections
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.{ArgumentParser, Namespace}
import sbt.internal.inc.classpath.ClassLoaderCache
import sbt.internal.inc.{PlainVirtualFile, PlainVirtualFileConverter, ZincUtil}
import scala.jdk.CollectionConverters.*

object DocRunner extends WorkerMain[Unit] {

  private class DocRequest private (
    val classpath: List[Path],
    val compilerBridge: Path,
    val compilerClasspath: List[Path],
    val logLevel: LogLevel,
    val sourceJars: List[Path],
    val options: List[String],
    val outputDir: Path,
    val sources: List[Path],
  )

  private object DocRequest {
    def apply(workDir: Path, namespace: Namespace): DocRequest = {
      new DocRequest(
        classpath = SandboxUtil.getSandboxPaths(workDir, namespace.getList[Path]("classpath")),
        compilerBridge = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("compiler_bridge")),
        compilerClasspath = SandboxUtil.getSandboxPaths(workDir, namespace.getList[Path]("compiler_classpath")),
        logLevel = LogLevel(namespace.getString("log_level")),
        options = Option(namespace.getList[String]("option")).map(_.asScala.toList).getOrElse(List.empty),
        outputDir = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("output_html")),
        sources = SandboxUtil.getSandboxPaths(workDir, namespace.getList[Path]("sources")),
        sourceJars = SandboxUtil.getSandboxPaths(workDir, namespace.getList[Path]("source_jars")),
      )
    }
  }

  private val classloaderCache = new ClassLoaderCache(new URLClassLoader(Array()))

  private val argParser: ArgumentParser = {
    val parser = ArgumentParsers.newFor("doc").addHelp(true).defaultFormatWidth(80).fromFilePrefix("@").build()
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
      .addArgument("--option")
      .help("option")
      .action(Arguments.append)
      .metavar("option")
    parser
      .addArgument("--log_level")
      .help("Log level")
      .choices(LogLevel.values.keys.toSeq: _*)
      .setDefault_(LogLevel.Warn.level)
    parser
      .addArgument("--source_jars")
      .help("Source jars")
      .metavar("path")
      .nargs("*")
      .`type`(PathArgumentType.apply())
      .setDefault_(Collections.emptyList)
    parser
      .addArgument("--output_html")
      .help("Output directory")
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

  override def init(args: Option[Array[String]]): Unit = ()

  override def work(task: WorkTask[Unit]): Unit = {
    val workRequest = DocRequest(
      task.workDir,
      ArgsUtil.parseArgsOrFailSafe(task.args, argParser, task.output),
    )
    InterruptUtil.throwIfInterrupted(task.isCancelled)

    val tmpDir = Files.createTempDirectory(task.workDir, "tmp")

    val sources = workRequest.sources ++
      workRequest.sourceJars.zipWithIndex
        .flatMap { case (jar, i) =>
          FileUtil.extractZip(jar, tmpDir.resolve("src").resolve(i.toString))
        }
        // Filter out MANIFEST files as they are not source files
        .filterNot(_.endsWith("META-INF/MANIFEST.MF"))

    val scalaInstance = AnnexScalaInstance.getAnnexScalaInstance(
      workRequest.compilerClasspath.view.map(_.toFile).toArray,
      task.workDir,
      isWorker,
    )

    val logger = new AnnexLogger(workRequest.logLevel, task.workDir, task.output)

    val scalaCompiler = ZincUtil
      .scalaCompiler(scalaInstance, workRequest.compilerBridge)
      .withClassLoaderCache(classloaderCache)

    InterruptUtil.throwIfInterrupted(task.isCancelled)

    val outputDir = workRequest.outputDir
    Files.createDirectories(outputDir)
    val reporter = new LoggedReporter(logger, scalaInstance.actualVersion)
    scalaCompiler.doc(
      sources.map(p => new PlainVirtualFile(p)),
      (scalaInstance.libraryJars.map(_.toPath).toList ++: workRequest.classpath).map(p => new PlainVirtualFile(p)),
      PlainVirtualFileConverter.converter,
      outputDir,
      workRequest.options,
      logger,
      reporter,
    )

    try {
      FileUtil.delete(tmpDir)
    } catch {
      case _: NoSuchFileException => {}
    }
    InterruptUtil.throwIfInterrupted(task.isCancelled)
  }
}
