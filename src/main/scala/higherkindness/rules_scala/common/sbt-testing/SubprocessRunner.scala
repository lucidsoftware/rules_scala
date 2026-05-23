package higherkindness.rules_scala.common.sbt_testing

import higherkindness.rules_scala.common.classloaders.ClassLoaders
import java.io.ObjectInputStream
import java.nio.file.Paths
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SubprocessTestRunner {

  def main(args: Array[String]): Unit = {
    val input = new ObjectInputStream(System.in)
    val request = input.readObject().asInstanceOf[TestRequest]
    val classLoader = ClassLoaders.sbtTestClassLoader(request.classpath.map(path => Paths.get(path).toUri.toURL))

    val loader = new TestFrameworkLoader(classLoader)
    val framework = loader.load(request.framework).get

    val passed = ClassLoaders.withContextClassLoader(classLoader) {
      TestHelper.withRunner(framework, request.scopeAndTestName, classLoader, request.testArgs) { runner =>
        val tasks = runner.tasks(Array(TestHelper.taskDef(request.test, request.scopeAndTestName)))
        tasks.length == 0 || {
          val reporter = new TestReporter(request.logger)

          // We're only running a single test class, so there's not much of a point in using the
          // `ConcurrentTestTaskExecutor`
          val taskExecutor = new SequentialTestTaskExecutor(request.logger)

          tasks.foreach(taskExecutor.submitTask)

          val result = Await.result(taskExecutor.waitForTasks(), Duration.Inf)

          !result.failures.nonEmpty
        }
      }
    }

    sys.exit(if (passed) 0 else 1)
  }

}
