package higherkindness.rules_scala
package workers.zinc.test

import common.classloaders.ClassLoaders
import common.args.implicits._
import common.sbt_testing.AnnexTestingLogger
import common.sbt_testing.TestDefinition
import common.sbt_testing.TestFrameworkLoader
import workers.common.AnalysisUtil
import java.io.File
import java.net.URLClassLoader
import java.nio.file.attribute.FileTime
import java.nio.file.{FileAlreadyExistsException, Files, Paths}
import java.time.Instant
import java.util.Collections
import java.util.regex.Pattern
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

object TestRunner {

  private[this] val argParser = {
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
      .choices("HIGH", "MEDIUM", "LOW")
      .setDefault_("MEDIUM")
    parser
      .addArgument("--framework_args")
      .help("Additional arguments for testing framework")
    parser
      .addArgument("-s")
      .help("Test class selector flag")
    parser
  }

  private[this] val testArgParser = {
    val parser = ArgumentParsers.newFor("test").addHelp(true).build()
    parser
      .addArgument("--analysis_store")
      .help("Analysis Store file")
      .metavar("class")
      .`type`(Arguments.fileType.verifyCanRead().verifyExists())
      .required(true)
    parser
      .addArgument("--subprocess_exec")
      .help("Executable for SubprocessTestRunner")
      .`type`(Arguments.fileType)
    parser
      .addArgument("--isolation")
      .choices("classloader", "none", "process")
      .help("Test isolation")
      .setDefault_("none")
    parser
      .addArgument("--frameworks")
      .help("Class names of sbt.testing.Framework implementations")
      .metavar("class")
      .nargs("*")
      .setDefault_(Collections.emptyList)
    parser
      .addArgument("--shared_classpath")
      .help("Classpath to share between tests")
      .metavar("path")
      .nargs("*")
      .`type`(Arguments.fileType)
      .setDefault_(Collections.emptyList)
    parser
      .addArgument("classpath")
      .help("Testing classpath")
      .metavar("path")
      .nargs("*")
      .`type`(Arguments.fileType.verifyCanRead().verifyExists())
      .setDefault_(Collections.emptyList)
    parser
  }

  def main(args: Array[String]): Unit = {
    // for ((name, value) <- sys.env) println(name + "=" + value)
    val namespace = argParser.parseArgsOrFail(args)

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
    val testNamespace = testArgParser.parseArgsOrFail(Files.readAllLines(testArgFile).asScala.toArray)

    val logger = new AnnexTestingLogger(namespace.getBoolean("color"), namespace.getString("verbosity"))

    val classpath = testNamespace
      .getList[File]("classpath")
      .asScala
      .map(file => runPath.resolve(file.toPath))
    val sharedClasspath = testNamespace
      .getList[File]("shared_classpath")
      .asScala
      .map(file => runPath.resolve(file.toPath))

    val sharedUrls = classpath.filter(sharedClasspath.toSet).map(_.toUri.toURL)

    val classLoader = ClassLoaders.sbtTestClassLoader(classpath.map(_.toUri.toURL).toSeq)
    val sharedClassLoader =
      ClassLoaders.sbtTestClassLoader(classpath.filter(sharedClasspath.toSet).map(_.toUri.toURL).toSeq)

    val analysisStoreFile = runPath.resolve(testNamespace.get[File]("analysis_store").toPath)
    val apis =
      try {
        AnalysisUtil
          .getAnalysis(
            AnalysisUtil.getAnalysisStore(
              analysisStoreFile.toFile,
              debug = false,
              isIncremental = false,
            ),
          )
          .apis
      } catch {
        case NonFatal(e) => throw new Exception(s"Failed to load APIs from analysis store: $analysisStoreFile", e)
      }

    val loader = new TestFrameworkLoader(classLoader, logger)
    val frameworks = testNamespace.getList[String]("frameworks").asScala.flatMap(loader.load)

    val testFilter = sys.env.get("TESTBRIDGE_TEST_ONLY").map(_.split("#", 2))
    val testClass = testFilter
      .map(_.head)
      .orElse(Option(namespace.getString("s")))
      .map(Pattern.compile)
    val testScopeAndName = testFilter.flatMap(_.lift(1))

    var count = 0
    val passed = frameworks.forall { framework =>
      val tests = new TestDiscovery(framework)(apis.internal.values.toSet).sortBy(_.name)
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
        val runner = testNamespace.getString("isolation") match {
          case "classloader" =>
            val urls = classpath.filterNot(sharedClasspath.toSet).map(_.toUri.toURL).toArray
            def classLoaderProvider() = new URLClassLoader(urls, sharedClassLoader)
            new ClassLoaderTestRunner(framework, classLoaderProvider _, logger)
          case "process" =>
            val executable = runPath.resolve(testNamespace.get[File]("subprocess_exec").toPath)
            val arguments = Option(namespace.getList[String]("subprocess_arg")).fold[Seq[String]](Nil)(_.asScala.toSeq)
            new ProcessTestRunner(
              framework,
              classpath.toSeq,
              new ProcessCommand(executable.toString, arguments),
              logger,
            )
          case "none" => new BasicTestRunner(framework, classLoader, logger)
        }
        val testFrameworkArguments =
          Option(namespace.getString("framework_args")).map(_.split("\\s+").toList).getOrElse(Seq.empty[String])
        try runner.execute(filteredTests, testScopeAndName.getOrElse(""), testFrameworkArguments)
        catch {
          case e: Throwable =>
            e.printStackTrace()
            false
        }
      }
    }
    sys.exit(if (passed) 0 else 1)
  }
}
