package higherkindness.rules_scala
package workers.zinc.compile

import common.args.ArgsUtil
import common.interrupt.InterruptUtil
import common.error.AnnexWorkerError
import common.worker.WorkerMain
import workers.common.{AnalysisUtil, AnnexLogger, AnnexMapper, AnnexScalaInstance, CommonArguments, FileUtil, LoggedReporter}
import com.google.devtools.build.buildjar.jarhelper.JarCreator
import java.io.{File, PrintStream, PrintWriter}
import java.net.URLClassLoader
import java.nio.file.{Files, Path, Paths}
import java.util
import java.util.{List as JList, Optional}
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.impl.Arguments as Arg
import net.sourceforge.argparse4j.inf.{ArgumentParserException, Namespace}
import sbt.internal.inc.classpath.ClassLoaderCache
import sbt.internal.inc.caching.ClasspathCache
import sbt.internal.inc.{Analysis, AnalyzingCompiler, CompileFailed, FilteredInfos, FilteredRelations, IncrementalCompilerImpl, Locate, PlainVirtualFile, PlainVirtualFileConverter, ZincUtil}
import scala.jdk.CollectionConverters.*
import scala.util.Try
import scala.util.control.NonFatal
import xsbti.{T2, VirtualFile, VirtualFileRef}
import xsbti.compile.{AnalysisContents, AnalysisStore, Changes, ClasspathOptionsUtil, CompileAnalysis, CompileOptions, CompileProgress, CompilerCache, DefaultExternalHooks, DefinesClass, ExternalHooks, FileHash, IncOptions, Inputs, MiniSetup, PerClasspathEntryLookup, PreviousResult, Setup, TastyFiles}

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

  private[this] val classloaderCache = new ClassLoaderCache(new URLClassLoader(Array()))

  private[this] val compilerCache = CompilerCache.fresh

  // prevents GC of the soft reference in classloaderCache
  private[this] var lastCompiler: AnyRef = null

  private[this] def labelToPath(label: String) = Paths.get(label.replaceAll("^/+", "").replaceAll(raw"[^\w/]", "_"))

  protected[this] def init(args: Option[Array[String]]): ZincRunnerWorkerConfig = {
    val parser = ArgumentParsers.newFor("zinc-worker").addHelp(true).build
    parser.addArgument("--persistence_dir", /* deprecated */ "--persistenceDir").metavar("path")
    parser.addArgument("--use_persistence").`type`(Arg.booleanType)
    parser.addArgument("--extracted_file_cache").metavar("path")
    // deprecated
    parser.addArgument("--max_errors")
    val namespace = parser.parseArgsOrFail(args.getOrElse(Array.empty))
    ZincRunnerWorkerConfig(namespace)
  }

  private[this] val parser = {
    val parser = ArgumentParsers.newFor("zinc").addHelp(true).defaultFormatWidth(80).fromFilePrefix("@").build()
    CommonArguments.add(parser)
  }

  protected[this] def work(
    workerConfig: ZincRunnerWorkerConfig,
    args: Array[String],
    out: PrintStream,
    workDir: Path,
    verbosity: Int,
  ): Unit = {
    val workRequest = CommonArguments(ArgsUtil.parseArgsOrFailSafe(args, parser, out), workDir)
    InterruptUtil.throwIfInterrupted()

    // These two paths must only be used when persistence is enabled because they escape the sandbox.
    // Sandboxing is disabled if persistence is enabled.
    val (persistenceDir, extractedFileCache) = if (workerConfig.usePersistence) {
      (workerConfig.persistenceDir, workerConfig.extractedFileCache)
    } else {
      (None, None)
    }

    val logger = new AnnexLogger(workRequest.logLevel, workDir, out)

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

    val deps = {
      val analyses: Map[Path, (Path, Path)] = {
        if (workerConfig.usePersistence) {
          workRequest.analyses.flatMap { analysis =>
            analysis.jars.map(jar =>
              jar -> (
                classesDir.resolve(labelToPath(analysis.label)),
                analysis.analysisStore,
              ),
            )
          }.toMap
        } else {
          Map.empty[Path, (Path, Path)]
        }
      }
      Dep.create(extractedFileCache, workRequest.classpath, analyses)
    }
    InterruptUtil.throwIfInterrupted()

    val debug = workRequest.debug
    val analysisStorePath = workRequest.outputAnalysisStore
    val readWriteMappers = AnnexMapper.mappers(workDir, workerConfig.usePersistence)
    val analysisStore: AnalysisStore = AnalysisUtil.getAnalysisStore(analysisStorePath.toFile, debug, readWriteMappers)

    val persistence = persistenceDir.fold[ZincPersistence](NullPersistence) { rootDir =>
      val path = workRequest.label.replaceAll("^/+", "").replaceAll(raw"[^\w/]", "_")
      new FilePersistence(rootDir.resolve(path), analysisStorePath, outputJar)
    }

    val classesOutputDir = classesDir.resolve(labelToPath(workRequest.label))
    try {
      persistence.load()
      if (Files.exists(outputJar)) {
        try FileUtil.extractZip(outputJar, classesOutputDir)
        catch {
          case NonFatal(e) =>
            FileUtil.delete(classesOutputDir)
            throw e
        }
      }
    } catch {
      case NonFatal(e) =>
        logger.warn(() => s"Failed to load cached analysis: $e")
        Files.delete(analysisStorePath)
    }
    Files.createDirectories(classesOutputDir)

    val previousResult = Try(analysisStore.get())
      .fold(
        { e =>
          logger.warn(() => s"Failed to load previous analysis: $e")
          Optional.empty[AnalysisContents]()
        },
        identity,
      )
      .map[PreviousResult](contents =>
        PreviousResult.of(Optional.of(contents.getAnalysis), Optional.of(contents.getMiniSetup)),
      )
      .orElseGet(() => PreviousResult.of(Optional.empty[CompileAnalysis](), Optional.empty[MiniSetup]()))

    // setup compiler
    val scalaInstance =
      AnnexScalaInstance.getAnnexScalaInstance(
        workRequest.compilerClasspath.view.map(_.toFile).toArray,
        workDir,
        isWorker,
      )

    val compileOptions =
      CompileOptions.create
        .withSources(sources.map(source => PlainVirtualFile(source.toAbsolutePath().normalize())).toArray)
        .withClasspath((classesOutputDir +: deps.map(_.classpath)).map(path => PlainVirtualFile(path)).toArray)
        .withClassesDirectory(classesOutputDir)
        .withJavacOptions(workRequest.javaCompilerOptions.toArray)
        .withScalacOptions(
          Array.concat(
            workRequest.plugins.map(p => s"-Xplugin:$p").toArray,
            workRequest.compilerOptions.toArray,
          ),
        )

    val compilers = {
      val scalaCompiler = ZincUtil
        .scalaCompiler(scalaInstance, workRequest.compilerBridge)
        .withClassLoaderCache(classloaderCache)
      lastCompiler = scalaCompiler
      ZincUtil.compilers(scalaInstance, ClasspathOptionsUtil.boot, None, scalaCompiler)
    }

    val lookup = {
      val depMap = deps.collect { case ExternalDep(_, depClasspath, depAnalysisStorePath) =>
        depClasspath -> depAnalysisStorePath
      }.toMap
      new AnxPerClasspathEntryLookup(file => {
        depMap
          .get(file)
          .map { analysisStorePath =>
            val analysis = AnalysisUtil.getAnalysis(
              AnalysisUtil.getAnalysisStore(
                analysisStorePath.toFile,
                debug,
                readWriteMappers,
              ),
            )
            Analysis.Empty.copy(
              apis = analysis.apis,
              relations = analysis.relations,
            )
          }
      })
    }

    val externalHooks = new DefaultExternalHooks(
      Optional.of(new DeterministicDirectoryHashExternalHooks()),
      Optional.empty(),
    )

    val setup = {
      val incOptions = IncOptions
        .create()
        .withAuxiliaryClassFiles(Array(TastyFiles.instance()))
        .withExternalHooks(externalHooks)
      val reporter = new LoggedReporter(logger, scalaInstance.actualVersion)
      val skip = false
      val file: Path = null
      Setup.create(
        lookup,
        skip,
        file,
        compilerCache,
        incOptions,
        reporter,
        Optional.empty[CompileProgress](),
        Array.empty[T2[String, String]],
      )
    }

    val inputs = Inputs.of(compilers, compileOptions, setup, previousResult)

    InterruptUtil.throwIfInterrupted()

    // compile
    val incrementalCompiler = new IncrementalCompilerImpl()
    val compileResult =
      try incrementalCompiler.compile(inputs, logger)
      catch {
        case _: CompileFailed => throw new AnnexWorkerError(-1)
        case e: ClassFormatError =>
          throw new Exception("You may be missing a `macro = True` attribute.", e)
          throw new AnnexWorkerError(1)
        case e: StackOverflowError => {
          // Downgrade to NonFatal error.
          // The JVM is not guaranteed to free shared resources correctly when unwinding the stack to catch a StackOverflowError,
          // but since we don't share resources between work threads, this should be mostly safe for us for now.
          // If Bazel could better handle the worker shutting down suddenly, we could allow this to be caught by
          // the UncaughtExceptionHandler in WorkerMain, and exit the entire process to be safe.
          throw new Error("StackOverflowError", e)
        }
      }

    InterruptUtil.throwIfInterrupted()

    // create analyses
    val pathString = analysisStorePath.toAbsolutePath().normalize().toString()
    val analysisStoreText = AnalysisUtil.getAnalysisStore(
      new File(pathString.substring(0, pathString.length() - 3) + ".text.gz"),
      true,
      readWriteMappers,
    )
    // Filter out libraryClassNames from the analysis because it is non-deterministic.
    // Can stop doing this once the bug in Zinc is fixed. Check the comment on FilteredRelations
    // for more info.
    val resultAnalysis = {
      val originalResultAnalysis = compileResult.analysis.asInstanceOf[Analysis]
      originalResultAnalysis.copy(
        relations = FilteredRelations.getFilteredRelations(originalResultAnalysis.relations),
        infos = FilteredInfos.getFilteredInfos(originalResultAnalysis.infos),
      )
    }
    analysisStoreText.set(AnalysisContents.create(resultAnalysis, compileResult.setup))
    analysisStore.set(AnalysisContents.create(resultAnalysis, compileResult.setup))

    // create used deps
    val usedDeps =
      // Filter out the Scala standard library as that should just always be
      // implicitly available and not something we should be book keeping.
      deps.filter(Dep.used(deps, resultAnalysis.relations, lookup)).filterNot { dep =>
        val filteredDepFileName = FileUtil.getNameWithoutRulesJvmExternalStampPrefix(dep.file)

        scalaInstance.libraryJars
          .map(FileUtil.getNameWithoutRulesJvmExternalStampPrefix)
          .contains(filteredDepFileName)
      }
    val writeMapper = readWriteMappers.getWriteMapper()
    Files.write(
      workRequest.outputUsed,
      // Send the used deps through the read write mapper, to strip the sandbox prefix and
      // make sure they're deterministic across machines
      usedDeps
        .map { usedDep =>
          writeMapper.mapClasspathEntry(usedDep.file).toString
        }
        .sorted
        .asJava,
    )

    // create jar
    val mains =
      resultAnalysis.infos.allInfos.values.toList
        .flatMap(_.getMainClasses.toList)
        .sorted

    val pw = new PrintWriter(workRequest.mainManifest.toFile)
    try mains.foreach(pw.println)
    finally pw.close()

    val jarCreator = new JarCreator(outputJar)
    jarCreator.addDirectory(classesOutputDir)
    jarCreator.setCompression(true)
    jarCreator.setNormalize(true)
    jarCreator.setVerbose(false)

    mains match {
      case main :: Nil =>
        jarCreator.setMainClass(main)
      case _ =>
    }

    jarCreator.execute()

    // save persisted files
    if (workerConfig.usePersistence) {
      try persistence.save()
      catch {
        case NonFatal(e) => logger.warn(() => s"Failed to save cached analysis: $e")
      }
    }

    // clear temporary files
    FileUtil.delete(tmpDir)
    Files.createDirectory(tmpDir)

    InterruptUtil.throwIfInterrupted()
  }
}

final class AnxPerClasspathEntryLookup(analyses: Path => Option[CompileAnalysis]) extends PerClasspathEntryLookup {
  override def analysis(classpathEntry: VirtualFile): Optional[CompileAnalysis] =
    analyses(PlainVirtualFileConverter.converter.toPath(classpathEntry))
      .fold(Optional.empty[CompileAnalysis])(Optional.of(_))
  override def definesClass(classpathEntry: VirtualFile): DefinesClass =
    Locate.definesClass(classpathEntry)
}

/**
 * We create this to deterministically set the hash code of directories otherwise they get set to the
 * System.identityHashCode() of an object created during compilation. That results in non-determinism.
 *
 * TODO: Get rid of this once the upstream fix is released:
 * https://github.com/sbt/zinc/commit/b4db1476d7fdb2c530a97c543ec9710c13ac58e3
 */
final class DeterministicDirectoryHashExternalHooks extends ExternalHooks.Lookup {
  // My understanding is that setting all these to None is the same as the
  // default behavior for external hooks, which provides an Optional.empty for
  // the external hooks.
  // The documentation for the getXYZ methods includes:
  // "None if is unable to determine what was changed, changes otherwise"
  // So I figure None is a safe bet here.
  override def getChangedSources(previousAnalysis: CompileAnalysis): Optional[Changes[VirtualFileRef]] =
    Optional.empty()
  override def getChangedBinaries(previousAnalysis: CompileAnalysis): Optional[util.Set[VirtualFileRef]] =
    Optional.empty()
  override def getRemovedProducts(previousAnalysis: CompileAnalysis): Optional[util.Set[VirtualFileRef]] =
    Optional.empty()

  // True here should be a safe default value, based on what I understand.
  // Here's why:
  //
  // There's a guard against this function returning an true incorrectly, so
  // I believe incremental compilation should still function correctly.
  // https://github.com/sbt/zinc/blob/f55b5b5abfba2dfcec0082b6fa8d329286803d2d/internal/zinc-core/src/main/scala/sbt/internal/inc/IncrementalCommon.scala#L186
  //
  // The only other place it's used is linked below. The default is an empty
  // Option, so forall will return true if an ExternalHooks.Lookup is not provided.
  // So this should be the same as default.
  // https://github.com/sbt/zinc/blob/f55b5b5abfba2dfcec0082b6fa8d329286803d2d/internal/zinc-core/src/main/scala/sbt/internal/inc/IncrementalCommon.scala#L429
  override def shouldDoIncrementalCompilation(
    changedClasses: util.Set[String],
    previousAnalysis: CompileAnalysis,
  ): Boolean = true

  // We set the hash code of the directories to 0. By default they get set
  // to the System.identityHashCode(), which is dependent on the current execution
  // of the JVM, so it is not deterministic.
  // If Zinc ever changes that behavior, we can get rid of this whole class.
  override def hashClasspath(classpath: Array[VirtualFile]): Optional[Array[FileHash]] = {
    val classpathArrayAsPaths = classpath.map(virtualFile => PlainVirtualFile.extractPath(virtualFile))
    val (directories, files) = classpathArrayAsPaths.partition(path => Files.isDirectory(path))
    val directoryFileHashes = directories.map { path =>
      FileHash.of(path, 0)
    }
    val fileHashes = ClasspathCache.hashClasspath(files.toSeq)

    Optional.of(directoryFileHashes ++ fileHashes)
  }
}
