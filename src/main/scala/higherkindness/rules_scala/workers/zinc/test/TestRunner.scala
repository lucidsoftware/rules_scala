package higherkindness.rules_scala.workers.zinc.test

import higherkindness.rules_scala.common.args.ArgsUtil.PathArgumentType
import higherkindness.rules_scala.common.args.implicits.*
import higherkindness.rules_scala.common.classloaders.ClassLoaders
import higherkindness.rules_scala.common.sandbox.SandboxUtil
import higherkindness.rules_scala.common.sbt_testing.{AnnexTestingLogger, ConcurrentTestTaskExecutor, SequentialTestTaskExecutor, TestDefinition, TestFrameworkLoader, TestsFileData, Verbosity}
import higherkindness.rules_scala.workers.zinc.test.TestRunner.Isolation
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
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.*
import scala.util.Using

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
    val isolation: Isolation,
    val sequential: Boolean,
    val sharedClasspath: List[Path],
    val subprocessExecutable: Option[Path],
    val testClasspath: List[Path],
    val testsFile: Path,
  )

  private object TestRunnerRequest {
    def apply(runPath: Path, namespace: Namespace): TestRunnerRequest = {
      new TestRunnerRequest(
        isolation = Isolation(namespace.getString("isolation")),
        sequential = namespace.getBoolean("sequential"),
        sharedClasspath = SandboxUtil.getSandboxPaths(runPath, namespace.getList[Path]("shared_classpath")),
        subprocessExecutable =
          Option(namespace.get[Path]("subprocess_exec")).map(SandboxUtil.getSandboxPath(runPath, _)),
        testClasspath = SandboxUtil.getSandboxPaths(runPath, namespace.getList[Path]("classpath")),
        testsFile = namespace.get[Path]("tests_file"),
      )
    }
  }

  private val testArgParser: ArgumentParser = {
    val parser = ArgumentParsers.newFor("test").addHelp(true).build()
    parser
      .addArgument("--isolation")
      .choices(Isolation.values.keys.toSeq: _*)
      .help("Test isolation")
      .setDefault_(Isolation.None.level)
    parser
      .addArgument("--sequential")
      .help("If passed, run test classes sequentially instead of concurrently.")
      .action(Arguments.storeTrue())
    parser
      .addArgument("--shared_classpath")
      .help("Classpath to share between tests")
      .metavar("path")
      .nargs("*")
      .`type`(PathArgumentType.apply())
      .setDefault_(Collections.emptyList)
    parser
      .addArgument("--subprocess_exec")
      .help("Executable for SubprocessTestRunner")
      .`type`(PathArgumentType.apply())
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
          if (testRunnerRequest.sequential && testRunnerRequest.isolation == Isolation.Process) {
            throw new Exception("Process isolation isn't yet compatible with sequential execution.")
          }

          val testTaskExecutor = if (testRunnerRequest.sequential) {
            new SequentialTestTaskExecutor(logger)
          } else {
            new ConcurrentTestTaskExecutor(logger)
          }

          val runner = testRunnerRequest.isolation match {
            case Isolation.ClassLoader =>
              val urls = testClasspath.filterNot(sharedClasspath.toSet).map(_.toUri.toURL).toArray
              def classLoaderProvider() = new URLClassLoader(urls, sharedClassLoader)
              new ClassLoaderTestRunner(framework, classLoaderProvider _, logger, testTaskExecutor)
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
            case Isolation.None => new BasicTestRunner(framework, classLoader, logger, testTaskExecutor)
          }

          try {
            Await.result(
              runner.execute(filteredTests.toList, testScopeAndName.getOrElse(""), testRunnerArgs.frameworkArgs),
              Duration.Inf,
            )
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
