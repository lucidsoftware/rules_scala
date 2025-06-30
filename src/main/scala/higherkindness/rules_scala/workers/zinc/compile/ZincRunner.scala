package higherkindness.rules_scala.workers.zinc.compile

import com.google.devtools.build.buildjar.jarhelper.JarCreator
import higherkindness.rules_scala.common.args.ArgsUtil
import higherkindness.rules_scala.common.error.AnnexWorkerError
import higherkindness.rules_scala.common.interrupt.InterruptUtil
import higherkindness.rules_scala.common.worker.{WorkTask, WorkerMain}
import higherkindness.rules_scala.workers.common.*
import java.io.{File, PrintWriter}
import java.net.URLClassLoader
import java.nio.file.{Files, Path, Paths}
import java.util.Optional
import javax.tools.{StandardLocation, ToolProvider}
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments as Arg
import net.sourceforge.argparse4j.inf.Namespace
import sbt.internal.inc.classfile.analyzeJavaClasses
import sbt.internal.inc.classpath.ClassLoaderCache
import sbt.internal.inc.javac.DiagnosticsReporter
import sbt.internal.inc.{CompileOutput, PlainVirtualFile, PlainVirtualFileConverter, ZincUtil}
import sbt.internal.util.LoggerWriter
import scala.jdk.CollectionConverters.*
import scala.util.control.NonFatal
import xsbti.compile.{DependencyChanges, ScalaInstance}
import xsbti.{AnalysisCallback, AnalysisCallback3, CompileFailed, Logger, Reporter, VirtualFile, VirtualFileRef}

class ZincRunnerWorkerConfig private (
  val persistenceDir: Option[Path],
  val usePersistence: Boolean,
  val extractedFileCache: Option[Path],
)

object ZincRunnerWorkerConfig {
  def apply(namespace: Namespace): ZincRunnerWorkerConfig = {
    new ZincRunnerWorkerConfig(
      pathFrom("persistence_dir", namespace),
      Option(namespace.getBoolean("use_persistence")).map(Boolean.unbox).getOrElse(false),
      pathFrom("extracted_file_cache", namespace),
    )
  }

  private def pathFrom(arg: String, namespace: Namespace): Option[Path] = {
    Option(namespace.getString(arg)).map { pathString =>
      if (pathString.startsWith("~" + File.separator)) {
        Paths.get(pathString.replace("~", sys.props.getOrElse("user.home", "")))
      } else if (pathString.startsWith("~")) {
        throw new Exception("Unsupported home directory expansion")
      } else {
        Paths.get(pathString)
      }
    }
  }
}

/**
 * <strong>Caching</strong>
 *
 * Zinc has two caches:
 *   1. a ClassLoaderCache which is a soft reference cache for classloaders of Scala compilers.
 *   1. a CompilerCache which is a hard reference cache for (I think) Scala compiler instances.
 *
 * The CompilerCache has reproducibility issues, so it needs to be a no-op. The ClassLoaderCache needs to be reused else
 * JIT reuse (i.e. the point of the worker strategy) doesn't happen.
 *
 * There are two sensible strategies for Bazel workers A. Each worker compiles multiple Scala versions. Trust the
 * ClassLoaderCache's timestamp check. Maintain a hard reference to the classloader for the last version, and allow
 * previous versions to be GC'ed subject to free memory and -XX:SoftRefLRUPolicyMSPerMB. B. Each worker compiles a
 * single Scala version. Probably still use ClassLoaderCache + hard reference since ClassLoaderCache is hard to remove.
 * The compiler classpath is passed via the initial flags to the worker (rather than the per-request arg file). Bazel
 * worker management cycles out Scala compiler versions. Currently, this runner follows strategy A.
 *
 * We use A in combination with having our own cache of AnnexScalaInstances, which is where we create the classloaders
 * that Zinc caches. We do so to prevent non-determinism in Zinc's analysis store files. Check the comments in
 * AnnexScalaInstance for more info.
 */
object ZincRunner extends WorkerMain[ZincRunnerWorkerConfig] {

  // Using Thread.interrupt to interrupt concurrent Zinc/Scala compilations that use a shared ScalaInstance (and thus
  // shared classloaders) can cause strange concurrency errors. To avoid those strange concurrency errors we only
  // cancel the FutureTask for the work task instead of cancel + Thread.interrupt. This makes cancellation more
  // cooperative. The work task can still check its cancellation status using isCancelled.
  // If you want to start using Thread.interrupt again, please make sure to test it very, very thoroughly using
  // dynamic execution. The concurrency error happens very rarely, so it's hard to reproduce.
  override protected val mayInterruptWorkerTasks = false

  private val classloaderCache = new ClassLoaderCache(new URLClassLoader(Array()))

  // prevents GC of the soft reference in classloaderCache
  private var lastCompiler: AnyRef = null
  private def compileScala(
    task: WorkTask[ZincRunnerWorkerConfig],
    parsedArguments: CommonArguments,
    scalaInstance: ScalaInstance,
    normalizedSources: Iterable[Path],
    classesOutputDirectory: Path,
    analysisCallback: AnalysisCallback3,
    reporter: Reporter,
    logger: Logger,
  ): Unit = try {
    val sourceVirtualFiles = normalizedSources.view.map(PlainVirtualFile(_): VirtualFile).toArray
    val classpath = parsedArguments.classpath.view.map(path => PlainVirtualFile(path): VirtualFile).toArray

    // Check if we should include the -sourceroot flag in the compiler options
    // We only include it for Scala 3 and later versions, as it is not supported
    // in Scala 2.x versions.
    // We include this so that the TASTy file generated by the Scala compiler
    // will be deterministic across machines and directories Bazel uses for
    // multiplexed sandbox execution.
    val shouldIncludeSourceRoot = !scalaInstance.actualVersion.startsWith("0.") &&
      scalaInstance.actualVersion.startsWith("3")

    val scalacOptions =
      parsedArguments.plugins.view.map(p => s"-Xplugin:$p").toArray ++
        parsedArguments.compilerOptions ++
        parsedArguments.compilerOptionsReferencingPaths.toArray ++
        (
          if (shouldIncludeSourceRoot) {
            Array("-sourceroot", task.workDir.toAbsolutePath.toString)
          } else {
            Array.empty[String]
          }
        )

    val scalaCompiler = ZincUtil
      .scalaCompiler(scalaInstance, parsedArguments.compilerBridge)
      .withClassLoaderCache(classloaderCache)

    lastCompiler = scalaCompiler

    InterruptUtil.throwIfInterrupted(task.isCancelled)

    scalaCompiler.compile(
      /*
       * We provide all the sources to the compiler, even Java sources. The Scala compiler isn't capable of compiling
       * Java, but it can parse it so Scala and Java can reference each other within the same compilation unit.
       *
       * For more information on how mixed compilation works, see:
       * https://github.com/sbt/zinc/blob/8b114cafbbeef9bc54b70c74990b74cd9bb20de3/internal/compiler-interface/src/main/java/xsbti/compile/CompileOrder.java
       */
      sourceVirtualFiles,
      classpath,
      PlainVirtualFileConverter.converter,
      changes = new DependencyChanges {
        override def isEmpty: Boolean = true
        override def modifiedBinaries(): Array[File] = Array.empty
        override def modifiedClasses(): Array[String] = Array.empty
        override def modifiedLibraries(): Array[VirtualFileRef] = Array.empty
      },
      scalacOptions,
      CompileOutput(classesOutputDirectory),
      analysisCallback,
      reporter,
      progressOpt = Optional.empty(),
      logger,
    )
  } catch {
    // The thread running this may have been interrupted during compilation due to a cancel request.
    // It's possible that the interruption contribute to the error. We should check if we were
    // interrupted, so we can respond with a cancellation rather than erroring and failing the build.
    case _: CompileFailed =>
      InterruptUtil.throwIfInterrupted(task.isCancelled)
      throw new AnnexWorkerError(-1)
    case e: ClassFormatError =>
      InterruptUtil.throwIfInterrupted(task.isCancelled)
      throw new AnnexWorkerError(1, "You may be missing a `macro = True` attribute.", e)
    case e: StackOverflowError =>
      // Downgrade to NonFatal error.
      // The JVM is not guaranteed to free shared resources correctly when unwinding the stack to catch a StackOverflowError,
      // but since we don't share resources between work threads, this should be mostly safe for us for now.
      // If Bazel could better handle the worker shutting down suddenly, we could allow this to be caught by
      // the UncaughtExceptionHandler in WorkerMain, and exit the entire process to be safe.
      InterruptUtil.throwIfInterrupted(task.isCancelled)
      throw new AnnexWorkerError(1, "StackOverflowError", e)
    case NonFatal(e) =>
      InterruptUtil.throwIfInterrupted(task.isCancelled)
      throw e
  }

  private def labelToPath(label: String) = Paths.get(label.replaceAll("^/+", "").replaceAll(raw"[^\w/]", "_"))
  private def maybeCompileJava(
    task: WorkTask[ZincRunnerWorkerConfig],
    parsedArguments: CommonArguments,
    normalizedSources: Iterable[Path],
    classesOutputDirectory: Path,
    analysisCallback: AnalysisCallback,
    reporter: Reporter,
    logger: Logger,
  ): Unit = {
    val javaSources = normalizedSources.view.map(_.toString).filter(_.endsWith(".java")).toList

    if (javaSources.nonEmpty) {
      val javaCompiler = ToolProvider.getSystemJavaCompiler
      val writer = new LoggerWriter(logger)
      val diagnosticListener = new DiagnosticsReporter(reporter)
      val fileManager = javaCompiler.getStandardFileManager(diagnosticListener, null, null)

      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List(classesOutputDirectory.toFile).asJava)

      val options = "-classpath" +:
        (classesOutputDirectory +: parsedArguments.classpath).map(_.toString).mkString(":") +:
        parsedArguments.javaCompilerOptions.toList

      val compilationTask = javaCompiler.getTask(
        writer,
        fileManager,
        diagnosticListener,
        options.asJava,
        null,
        fileManager.getJavaFileObjects(javaSources*),
      )

      val wasSuccessful =
        try {
          compilationTask.call()
        } catch {
          case NonFatal(exception) =>
            InterruptUtil.throwIfInterrupted(task.isCancelled)

            throw exception
        }

      if (!wasSuccessful) {
        InterruptUtil.throwIfInterrupted(task.isCancelled)

        throw new AnnexWorkerError(1)
      }

      InterruptUtil.throwIfInterrupted(task.isCancelled)

      analyzeJavaClasses(
        normalizedSources.view.map(PlainVirtualFile(_)).toList,
        parsedArguments.classpath,
        classesOutputDirectory,
        logger,
        analysisCallback,
      )
    }
  }

  protected def init(args: Option[Array[String]]): ZincRunnerWorkerConfig = {
    val parser = ArgumentParsers.newFor("zinc-worker").addHelp(true).build
    parser.addArgument("--persistence_dir", /* deprecated */ "--persistenceDir").metavar("path")
    parser.addArgument("--use_persistence").`type`(Arg.booleanType)
    parser.addArgument("--extracted_file_cache").metavar("path")
    // deprecated
    parser.addArgument("--max_errors")
    val namespace = parser.parseArgsOrFail(args.getOrElse(Array.empty))
    ZincRunnerWorkerConfig(namespace)
  }

  private val parser = {
    val parser = ArgumentParsers.newFor("zinc").addHelp(true).defaultFormatWidth(80).fromFilePrefix("@").build()
    CommonArguments.add(parser)
  }

  protected def work(task: WorkTask[ZincRunnerWorkerConfig]): Unit = {
    val workRequest = CommonArguments(
      ArgsUtil.parseArgsOrFailSafe(task.args, parser, task.output),
      task.workDir,
    )
    InterruptUtil.throwIfInterrupted(task.isCancelled)

    // These two paths must only be used when persistence is enabled because they escape the sandbox.
    // Sandboxing is disabled if persistence is enabled.
    val (persistenceDir, extractedFileCache) = if (task.context.usePersistence) {
      (task.context.persistenceDir, task.context.extractedFileCache)
    } else {
      (None, None)
    }

    val logger = new AnnexLogger(workRequest.logLevel, task.workDir, task.output)

    val tmpDir = workRequest.tmpDir

    // extract srcjars
    val sources = {
      val sourcesDir = tmpDir.resolve("src")
      workRequest.sources ++
        workRequest.sourceJars.zipWithIndex
          .flatMap { case (jar, i) =>
            FileUtil.extractZip(jar, sourcesDir.resolve(i.toString))
          }
          // Filter out MANIFEST files as they are not source files
          .filterNot(_.endsWith("META-INF/MANIFEST.MF"))
    }

    // extract upstream classes
    val classesDir = tmpDir.resolve("classes")
    val outputJar = workRequest.outputJar
    val readWriteMappers = AnnexMapper.mappers(task.workDir, task.context.usePersistence)
    val classesOutputDir = classesDir.resolve(labelToPath(workRequest.label))
    Files.createDirectories(classesOutputDir)

    val scalaInstance = AnnexScalaInstance
      .getAnnexScalaInstance(workRequest.compilerClasspath.view.map(_.toFile).toArray, task.workDir, isWorker)

    val normalizedSources = sources.view.map(_.toAbsolutePath.normalize()).toArray
    val reporter = new LoggedReporter(logger, scalaInstance.actualVersion)

    InterruptUtil.throwIfInterrupted(task.isCancelled)

    val analysis = AnnexAnalysis()
    val analysisCallback = analysis.getCallback(PlainVirtualFileConverter.converter)

    compileScala(
      task,
      workRequest,
      scalaInstance,
      normalizedSources,
      classesOutputDir,
      analysisCallback,
      reporter,
      logger,
    )

    InterruptUtil.throwIfInterrupted(task.isCancelled)

    maybeCompileJava(
      task,
      workRequest,
      normalizedSources,
      classesOutputDir,
      analysisCallback,
      reporter,
      logger,
    )

    InterruptUtil.throwIfInterrupted(task.isCancelled)

    // create used deps
    val scalaStandardLibraryJars = scalaInstance.libraryJars.view
      .map(file => Paths.get(FileUtil.getNameWithoutRulesJvmExternalStampPrefix(file)))
      .toSet

    val usedDeps = workRequest.classpath.view
      .map(_.normalize().toAbsolutePath)
      .toSet
      .intersect(analysis.usedJars.toSet)
      // Filter out the Scala standard library as they should always be implicitly available we shouldn't be
      // bookkeeping them
      .view
      .filter { path =>
        val filteredPath = Paths.get(FileUtil.getNameWithoutRulesJvmExternalStampPrefix(path))

        !scalaStandardLibraryJars.contains(filteredPath)
      }
      .toList
    val writeMapper = readWriteMappers.getWriteMapper()
    Files.write(
      workRequest.outputUsed,
      // Send the used deps through the read write mapper, to strip the sandbox prefix and
      // make sure they're deterministic across machines
      usedDeps.view
        .map(writeMapper.mapClasspathEntry(_).toString)
        .toList
        .sorted
        .asJava,
    )

    // create jar
    val mains = analysis.mainClasses.toArray
    val pw = new PrintWriter(workRequest.mainManifest.toFile)
    try {
      mains.foreach(pw.println)
    } finally {
      pw.close()
    }

    val jarCreator = new JarCreator(outputJar)
    jarCreator.addDirectory(classesOutputDir)
    jarCreator.setCompression(true)
    jarCreator.setNormalize(true)
    jarCreator.setVerbose(false)

    mains match {
      case Array(main) =>
        jarCreator.setMainClass(main)
      case _ =>
    }

    jarCreator.execute()

    // TODO: Do this properly
    val analysisStorePathString = workRequest.outputAnalysisStore.toString
    val analysisStoreTextPath =
      Paths.get(s"${analysisStorePathString.slice(0, analysisStorePathString.length - 3)}.text.gz")

    Files.createFile(workRequest.outputAnalysisStore)
    Files.createFile(analysisStoreTextPath)

    // clear temporary files
    FileUtil.delete(tmpDir)
    Files.createDirectory(tmpDir)

    InterruptUtil.throwIfInterrupted(task.isCancelled)
  }
}
