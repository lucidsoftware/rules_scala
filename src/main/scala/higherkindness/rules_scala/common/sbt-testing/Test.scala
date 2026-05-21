package higherkindness.rules_scala.common.sbt_testing

import java.nio.file.{Path, Paths}
import play.api.libs.json.{Format, Json}
import sbt.testing.{Framework, Logger, Runner, Task, TaskDef, TestWildcardSelector}
import scala.collection.mutable
import scala.util.control.NonFatal

case class TestDefinition(name: String, fingerprint: TestFingerprint)

object TestDefinition {
  implicit val format: Format[TestDefinition] = Json.format[TestDefinition]
}

class TestFrameworkLoader(loader: ClassLoader) {
  private val loadedJarsByFramework = mutable.Map.empty[String, mutable.ArrayBuffer[Path]]
  private def getClassJar(`class`: Class[?]): Option[Path] = for {
    codeSource <- Option(`class`.getProtectionDomain().getCodeSource())
    codeSourceUri = codeSource.getLocation().toURI()
    path <-
      try {
        Some(Paths.get(codeSourceUri))
      } catch {
        case _: IllegalArgumentException => None
      }
  } yield path

  def getLoadedJarsByFramework(): Map[String, Array[Path]] =
    loadedJarsByFramework.view.map { case (frameworkName, loadedJars) => frameworkName -> loadedJars.toArray }.toMap

  def load(className: String) = {
    val (framework, loadedJar) =
      try {
        val `class` = Class.forName(className, true, loader)
        val loadedJar = getClassJar(`class`)

        (Some(`class`.getDeclaredConstructor().newInstance()), loadedJar)
      } catch {
        case _: ClassNotFoundException => (None, None)
        case NonFatal(e)               => throw new Exception(s"Failed to load framework $className", e)
      }
    framework.map {
      case framework: Framework =>
        loadedJar.foreach(loadedJarsByFramework.getOrElseUpdate(className, mutable.ArrayBuffer.empty) += _)

        framework

      case _ => throw new Exception(s"$className does not implement ${classOf[Framework].getName}")
    }

  }
}

object TestHelper {
  def withRunner[A](framework: Framework, scopeAndTestName: String, classLoader: ClassLoader, arguments: Seq[String])(
    f: Runner => A,
  ) = {
    val options =
      if (framework.name == "specs2") {
        // scopeAndTestName from intellij has the format '\Q${scope}::${test}\E$' for individual tests,
        // and '\Q${scope}\E::' for scopes (see getTestFilter in com.google.idea.blaze.scala.run.Specs2Utils).
        // specs2 filters only on test, not on scope. If we get just a scope, pass nothing.
        // If we get a test name (with or without a `${scope}::` prefix), give it to specs2.
        val testName = scopeAndTestName.replaceFirst(".*?::", "")
        if (testName.nonEmpty) {
          // \Q is the only thing from scope that we _don't_ want to drop (since it's not actually part of the scope,
          // it's part of the overall regex)
          val prefix = if (scopeAndTestName.startsWith(raw"\Q") && !testName.startsWith(raw"\Q")) raw"\Q" else ""
          Array("-ex", prefix + testName)
        } else {
          Array.empty[String]
        }
      } else Array.empty[String]
    val runner = framework.runner(arguments.toArray, options, classLoader)
    try f(runner)
    finally runner.done()
  }

  def taskDef(test: TestDefinition, scopeAndTestName: String) =
    new TaskDef(
      test.name,
      test.fingerprint,
      false,
      Array(new TestWildcardSelector(scopeAndTestName.replace("::", " "))),
    )
}

class TestReporter(logger: Logger) {
  def post(failures: Iterable[String]) = if (failures.nonEmpty) {
    logger.error(s"${failures.size} ${if (failures.size == 1) "failure" else "failures"}:")
    failures.toSeq.sorted.foreach(name => logger.error(s"    $name"))
    logger.error("")
  }

  def postTask() = logger.info("")

  def pre(framework: Framework, tasks: Iterable[Task]) = {
    logger.info(s"${framework.getClass.getName}: ${tasks.size} tests")
    logger.info("")
  }

  def preTask(task: Task) = logger.info(task.taskDef.fullyQualifiedName)
}
