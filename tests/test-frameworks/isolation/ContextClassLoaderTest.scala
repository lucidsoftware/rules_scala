import higherkindness.rules_scala.common.sbt_testing.ConcurrentTestTaskExecutor
import org.specs2.mutable.Specification
import sbt.testing.{EventHandler, Logger, Selector, SubclassFingerprint, Task, TaskDef}
import scala.concurrent.{Await, Promise}
import scala.concurrent.duration.*

object ContextClassLoaderTest extends Specification {
  "Concurrent test execution" should {
    "run each task with the context classloader used to submit it" in {
      val logger = new Logger {
        def ansiCodesSupported(): Boolean = false
        def error(message: String): Unit = ()
        def warn(message: String): Unit = ()
        def info(message: String): Unit = ()
        def debug(message: String): Unit = ()
        def trace(error: Throwable): Unit = ()
      }
      val fingerprint = new SubclassFingerprint {
        def isModule(): Boolean = true
        def superclassName(): String = "java.lang.Object"
        def requireNoArgConstructor(): Boolean = false
      }
      val executor = new ConcurrentTestTaskExecutor(logger)
      val thread = Thread.currentThread()
      val previousClassLoader = thread.getContextClassLoader
      val classLoaders = List(new ClassLoader(previousClassLoader) {}, new ClassLoader(previousClassLoader) {})
      val results =
        try {
          classLoaders.map { classLoader =>
            val observed = Promise[ClassLoader]()
            thread.setContextClassLoader(classLoader)
            executor.submitTask(new Task {
              def tags(): Array[String] = Array.empty
              def taskDef(): TaskDef = new TaskDef("context", fingerprint, false, Array.empty[Selector])
              def execute(handler: EventHandler, loggers: Array[Logger]): Array[Task] = {
                observed.success(Thread.currentThread().getContextClassLoader)
                Array.empty
              }
            })
            observed.future
          }
        } finally {
          thread.setContextClassLoader(previousClassLoader)
        }
      Await.result(executor.waitForTasks(), 10.seconds)
      results.map(result => Await.result(result, 10.seconds)) mustEqual classLoaders
    }
  }
}
