package higherkindness.rules_scala.workers.zinc.test

import higherkindness.rules_scala.common.args.ArgsUtil.PathArgumentType
import higherkindness.rules_scala.common.args.implicits.*
import higherkindness.rules_scala.common.classloaders.ClassLoaders
import higherkindness.rules_scala.common.sandbox.SandboxUtil
import higherkindness.rules_scala.common.sbt_testing.{AnnexTestingLogger, TestDefinition, TestFrameworkLoader, TestsFileData, Verbosity}
import higherkindness.rules_scala.workers.common.AnalysisUtil
import java.io.FileInputStream
import java.net.URLClassLoader
import java.nio.file.attribute.FileTime
import java.nio.file.{FileAlreadyExistsException, Files, Path, Paths}
import java.time.Instant
import java.util.Collections
import java.util.regex.Pattern
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.{ArgumentParser, Namespace}
import play.api.libs.json.Json
import scala.jdk.CollectionConverters.*
import scala.util.Using
import scala.util.control.NonFatal

object TestRunner {
  private sealed abstract class Isolation(val level: String)
  private object Isolation {
    case object ClassLoader extends Isolation("classloader")
    case object None extends Isolation("none")
    case object Process extends Isolation("process")
    val values = Map(
      ClassLoader.level -> ClassLoader,
      None.level -> None,
      Process.level -> Process,
    )

    def apply(level: String): Isolation = values(level)
  }

  private class TestRunnerArgs private (
    val color: Boolean,
    val subprocessArgs: List[String],
    val verbosity: Verbosity,
    val frameworkArgs: List[String],
    val testClassSelector: Option[String],
  )

  private object TestRunnerArgs {
    def apply(namespace: Namespace): TestRunnerArgs = {
      new TestRunnerArgs(
        color = namespace.getBoolean("color"),
        subprocessArgs =
          Option(namespace.getList[String]("subprocess_arg")).map(_.asScala.toList).getOrElse(List.empty),
        verbosity = Verbosity(namespace.getString("verbosity")),
        frameworkArgs = Option(namespace.getString("framework_args")).map(_.split("\\s+").toList).getOrElse(List.empty),
        testClassSelector = Option(namespace.getString("s")),
      )
    }
  }

  private val argParser: ArgumentParser = {
    val parser = ArgumentParsers.newFor("test-runner").addHelp(true).fromFilePrefix("@").build()
    parser.description("Run tests")
    parser
      .addArgument("--color")
      .help("ANSI color")
      .metavar("class")
      .`type`(Arguments.booleanType)
      .setDefault_(true)
    parser
      .addArgument("--subprocess_arg")
      .action(Arguments.append)
      .help("Argument for tests run in new JVM process")
    parser
      .addArgument("--verbosity")
      .help("Verbosity")
      .choices(Verbosity.values.keys.toSeq: _*)
      .setDefault_(Verbosity.Medium.level)
    parser
      .addArgument("--framework_args")
      .help("Additional arguments for testing framework")
    parser
      .addArgument("-s")
      .help("Test class selector flag")
    parser
  }

  private class TestRunnerRequest private (
    val analysisStore: Path,
    val subprocessExecutable: Option[Path],
    val isolation: Isolation,
    val sharedClasspath: List[Path],
    val testClasspath: List[Path],
    val testsFile: Path,
  )

  private object TestRunnerRequest {
    def apply(runPath: Path, namespace: Namespace): TestRunnerRequest = {
      new TestRunnerRequest(
        analysisStore = SandboxUtil.getSandboxPath(runPath, namespace.get[Path]("analysis_store")),
        subprocessExecutable =
          Option(namespace.get[Path]("subprocess_exec")).map(SandboxUtil.getSandboxPath(runPath, _)),
        isolation = Isolation(namespace.getString("isolation")),
        sharedClasspath = SandboxUtil.getSandboxPaths(runPath, namespace.getList[Path]("shared_classpath")),
        testClasspath = SandboxUtil.getSandboxPaths(runPath, namespace.getList[Path]("classpath")),
        testsFile = namespace.get[Path]("tests_file"),
      )
    }
  }

  private val testArgParser: ArgumentParser = {
    val parser = ArgumentParsers.newFor("test").addHelp(true).build()
    parser
      .addArgument("--analysis_store")
      .help("Analysis Store file")
      .metavar("class")
      .`type`(PathArgumentType.apply())
      .required(true)
    parser
      .addArgument("--subprocess_exec")
      .help("Executable for SubprocessTestRunner")
      .`type`(PathArgumentType.apply())
    parser
      .addArgument("--isolation")
      .choices(Isolation.values.keys.toSeq: _*)
      .help("Test isolation")
      .setDefault_(Isolation.None.level)
    parser
      .addArgument("--shared_classpath")
      .help("Classpath to share between tests")
      .metavar("path")
      .nargs("*")
      .`type`(PathArgumentType.apply())
      .setDefault_(Collections.emptyList)
    parser
      .addArgument("--tests_file")
      .help("File containing discovered tests.")
      .metavar("file")
      .`type`(PathArgumentType.apply())
    parser
      .addArgument("classpath")
      .help("Testing classpath")
      .metavar("path")
      .nargs("*")
      .`type`(PathArgumentType.apply())
      .setDefault_(Collections.emptyList)
    parser
  }

  def main(args: Array[String]): Unit = {
    val testRunnerArgs = TestRunnerArgs(argParser.parseArgsOrFail(args))

    sys.env.get("TEST_SHARD_STATUS_FILE").map { path =>
      val file = Paths.get(path)
      try Files.createFile(file)
      catch {
        case _: FileAlreadyExistsException =>
          Files.setLastModifiedTime(file, FileTime.from(Instant.now))
      }
    }

    val runPath = Paths.get(sys.props("bazel.runPath"))
    val testArgFile = Paths.get(sys.props("scalaAnnex.test.args"))
    val testRunnerRequest =
      TestRunnerRequest(runPath, testArgParser.parseArgsOrFail(Files.readAllLines(testArgFile).asScala.toArray))

    val logger = new AnnexTestingLogger(testRunnerArgs.color, testRunnerArgs.verbosity)

    val testClasspath = testRunnerRequest.testClasspath
    val sharedClasspath = testRunnerRequest.sharedClasspath
    val sharedUrls = testClasspath.filter(sharedClasspath.toSet).map(_.toUri.toURL)

    val classLoader = ClassLoaders.sbtTestClassLoader(testClasspath.map(_.toUri.toURL).toSeq)
    val sharedClassLoader = ClassLoaders.sbtTestClassLoader(sharedUrls)

    val apis =
      try {
        AnalysisUtil
          .getAnalysis(
            AnalysisUtil.getAnalysisStore(
              testRunnerRequest.analysisStore.toFile,
              debug = false,
              isIncremental = false,
              // There's no sandboxing here because this isn't a worker, so just use an empty path
              // for the sandbox prefix.
              workDir = Paths.get(""),
            ),
          )
          .apis
      } catch {
        case NonFatal(e) =>
          throw new Exception(s"Failed to load APIs from analysis store: ${testRunnerRequest.analysisStore}", e)
      }

    val loader = new TestFrameworkLoader(classLoader)
    val testsFileData = Using(new FileInputStream(testRunnerRequest.testsFile.toString)) { stream =>
      Json.fromJson[TestsFileData](Json.parse(stream)).get
    }.get

    val testFilter = sys.env.get("TESTBRIDGE_TEST_ONLY").map(_.split("#", 2))
    val testClass = testFilter
      .map(_.head)
      .orElse(testRunnerArgs.testClassSelector)
      .map(Pattern.compile)
    val testScopeAndName = testFilter.flatMap(_.lift(1))

    var count = 0
    val passed = testsFileData.testsByFramework.view
      .flatMap { case (frameworkName, tests) => loader.load(frameworkName).map((frameworkName, _, tests)) }
      .forall { case (frameworkName, framework, tests) =>
        val filter = for {
          index <- sys.env.get("TEST_SHARD_INDEX").map(_.toInt)
          total <- sys.env.get("TEST_TOTAL_SHARDS").map(_.toInt)
        } yield (test: TestDefinition, i: Int) => i % total == index
        val filteredTests = tests.filter { test =>
          testClass.forall(_.matcher(test.name).matches) && {
            count += 1
            filter.fold(true)(_(test, count))
          }
        }
        filteredTests.isEmpty || {
          val runner = testRunnerRequest.isolation match {
            case Isolation.ClassLoader =>
              val urls = testClasspath.filterNot(sharedClasspath.toSet).map(_.toUri.toURL).toArray
              def classLoaderProvider() = new URLClassLoader(urls, sharedClassLoader)
              new ClassLoaderTestRunner(framework, classLoaderProvider _, logger)
            case Isolation.Process =>
              val executable = testRunnerRequest.subprocessExecutable.map(_.toString).getOrElse {
                throw new Exception("Subprocess executable missing for test ran in process isolation mode.")
              }
              new ProcessTestRunner(
                framework,
                testClasspath.toSeq,
                new ProcessCommand(executable, testRunnerArgs.subprocessArgs),
                logger,
              )
            case Isolation.None => new BasicTestRunner(framework, classLoader, logger)
          }

          try {
            runner.execute(filteredTests.toList, testScopeAndName.getOrElse(""), testRunnerArgs.frameworkArgs)
          } catch {
            case e: Throwable =>
              e.printStackTrace()
              false
          }
        }
      }
    sys.exit(if (passed) 0 else 1)
  }
}
