package higherkindness.rules_scala.workers.zinc.test

import higherkindness.rules_scala.common.classloaders.ClassLoaders
import higherkindness.rules_scala.common.sbt_testing.JUnitXmlReporter
import higherkindness.rules_scala.common.sbt_testing.TestDefinition
import higherkindness.rules_scala.common.sbt_testing.TestFrameworkLoader
import higherkindness.rules_scala.common.sbt_testing.TestHelper
import higherkindness.rules_scala.common.sbt_testing.TestReporter
import higherkindness.rules_scala.common.sbt_testing.TestRequest
import higherkindness.rules_scala.common.sbt_testing.TestTaskExecutor
import java.io.ObjectOutputStream
import java.nio.file.Path
import sbt.testing.{Framework, Logger}
import scala.collection.mutable
import scala.concurrent.{blocking, ExecutionContext, Future}

class BasicTestRunner(
  framework: Framework,
  classLoader: ClassLoader,
  logger: Logger,
  testTaskExecutor: TestTaskExecutor,
) extends TestFrameworkRunner {
  def execute(tests: List[TestDefinition], scopeAndTestName: String, arguments: List[String]): Future[Boolean] = {
    ClassLoaders.withContextClassLoader(classLoader) {
      TestHelper.withRunner(framework, scopeAndTestName, classLoader, arguments) { runner =>
        val reporter = new TestReporter(logger)
        val tasks = runner.tasks(tests.map(TestHelper.taskDef(_, scopeAndTestName)).toArray)
        reporter.pre(framework, tasks)
        tasks.foreach(testTaskExecutor.submitTask)
        testTaskExecutor
          .waitForTasks()
          .map { result =>
            blocking {
              reporter.post(result.failures)

              val xmlReporter = new JUnitXmlReporter(result.taskEvents)

              xmlReporter.write()

              !result.failures.nonEmpty
            }
          }(using ExecutionContext.global)
      }
    }
  }
}

class ClassLoaderTestRunner(
  framework: Framework,
  classLoaderProvider: () => ClassLoader,
  logger: Logger,
  testTaskExecutor: TestTaskExecutor,
) extends TestFrameworkRunner {
  def execute(tests: List[TestDefinition], scopeAndTestName: String, arguments: List[String]): Future[Boolean] = {
    val reporter = new TestReporter(logger)

    val classLoader = framework.getClass.getClassLoader
    ClassLoaders.withContextClassLoader(classLoader) {
      TestHelper.withRunner(framework, scopeAndTestName, classLoader, arguments) { runner =>
        val tasks = runner.tasks(tests.map(TestHelper.taskDef(_, scopeAndTestName)).toArray)
        reporter.pre(framework, tasks)
      }
    }

    tests.foreach { test =>
      val classLoader = classLoaderProvider()
      val isolatedFramework = new TestFrameworkLoader(classLoader).load(framework.getClass.getName).get
      TestHelper.withRunner(isolatedFramework, scopeAndTestName, classLoader, arguments) { runner =>
        ClassLoaders.withContextClassLoader(classLoader) {
          val tasks = runner.tasks(Array(TestHelper.taskDef(test, scopeAndTestName)))
          tasks.foreach(testTaskExecutor.submitTask)
        }
      }
    }

    testTaskExecutor
      .waitForTasks()
      .map { result =>
        blocking {
          reporter.post(result.failures)

          val xmlReporter = new JUnitXmlReporter(result.taskEvents)

          xmlReporter.write()

          !result.failures.nonEmpty
        }
      }(using ExecutionContext.global)
  }
}

class ProcessCommand(
  val executable: String,
  val arguments: List[String],
) extends Serializable

class ProcessTestRunner(
  framework: Framework,
  classpath: List[Path],
  command: ProcessCommand,
  logger: Logger & Serializable,
) extends TestFrameworkRunner {
  def execute(tests: List[TestDefinition], scopeAndTestName: String, arguments: List[String]): Future[Boolean] = {
    val reporter = new TestReporter(logger)

    val classLoader = framework.getClass.getClassLoader
    ClassLoaders.withContextClassLoader(classLoader) {
      TestHelper.withRunner(framework, scopeAndTestName, classLoader, arguments) { runner =>
        val tasks = runner.tasks(tests.map(TestHelper.taskDef(_, scopeAndTestName)).toArray)
        reporter.pre(framework, tasks)
      }
    }

    val failures = mutable.Set[String]()
    tests.foreach { test =>
      val process = new ProcessBuilder((command.executable +: command.arguments)*)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .start()
      try {
        val request = new TestRequest(
          framework.getClass.getName,
          test,
          scopeAndTestName,
          classpath.map(_.toString),
          logger,
          arguments,
        )
        val out = new ObjectOutputStream(process.getOutputStream)
        try out.writeObject(request)
        finally out.close()
        if (process.waitFor() != 0) {
          failures += test.name
        }
      } finally process.destroy
    }
    reporter.post(failures)
    Future.successful(!failures.nonEmpty) // We don't yet support concurrent execution with process isolation
  }
}

trait TestFrameworkRunner {
  def execute(tests: List[TestDefinition], scopeAndTestName: String, arguments: List[String]): Future[Boolean]
}
