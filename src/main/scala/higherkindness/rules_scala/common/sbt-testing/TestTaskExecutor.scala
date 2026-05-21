package higherkindness.rules_scala.common.sbt_testing

import java.util.concurrent.ConcurrentLinkedQueue
import sbt.testing.{Event, Logger, Status, Task}
import scala.collection.{concurrent, mutable}
import scala.concurrent.{blocking, ExecutionContext, Future}
import scala.jdk.CollectionConverters.*

case class TaskExecutorResult(taskEvents: Map[Task, Array[Event]], failures: Array[String])

private object TaskExecutorResult {
  private[sbt_testing] case class Mutable(
    taskEvents: concurrent.TrieMap[Task, ConcurrentLinkedQueue[Event]],
    failures: ConcurrentLinkedQueue[String],
  ) {
    def clear(): Unit = {
      taskEvents.clear()
      failures.clear()
    }

    def toTaskExecutorResult: TaskExecutorResult = TaskExecutorResult(
      taskEvents.view.map { case task -> events => task -> events.asScala.toArray }.toMap,
      failures.asScala.toArray,
    )
  }

  private[sbt_testing] object Mutable {
    def empty: Mutable = apply(concurrent.TrieMap.empty, new ConcurrentLinkedQueue())
  }
}

trait TestTaskExecutor {
  def submitTask(task: Task): Unit
  def waitForTasks(): Future[TaskExecutorResult]
}

class ConcurrentTestTaskExecutor(logger: Logger) extends TestTaskExecutor {
  private val activeTasks = new ConcurrentLinkedQueue[Future[Unit]]()
  private val currentResult = TaskExecutorResult.Mutable.empty

  override def submitTask(task: Task): Unit = activeTasks.add(
    Future {
      blocking {
        val bufferedLogger = new BufferedLogger(logger)
        val reporter = new TestReporter(bufferedLogger)

        reporter.preTask(task)

        val additionalTasks = task.execute(
          event => {
            currentResult.synchronized {
              currentResult.taskEvents.getOrElseUpdate(task, new ConcurrentLinkedQueue()).add(event)

              event.status match {
                case Status.Failure | Status.Error => currentResult.failures.add(task.taskDef.fullyQualifiedName)
                case _                             =>
              }
            }
          },
          Array(new PrefixedTestingLogger(bufferedLogger, "    ")),
        )

        additionalTasks.foreach(submitTask)

        reporter.postTask()

        // Only one task should write to stderr/stdout at a time. Of course, the task implementation could write to
        // stdout/stderr directly, but that's out of our control.
        synchronized {
          bufferedLogger.flush()
        }
      }
    }(ExecutionContext.global),
  )

  override def waitForTasks(): Future[TaskExecutorResult] = {
    given ExecutionContext = ExecutionContext.global

    Future
      .sequence(activeTasks.asScala)
      .map { _ =>
        activeTasks.clear()
        currentResult.toTaskExecutorResult
      }(ExecutionContext.global)
  }
}

class SequentialTestTaskExecutor(logger: Logger) extends TestTaskExecutor {
  private val currentResult = TaskExecutorResult.Mutable.empty

  override def submitTask(task: Task): Unit = {
    val reporter = new TestReporter(logger)

    reporter.preTask(task)

    val additionalTasks = task.execute(
      event => {
        currentResult.taskEvents.getOrElseUpdate(task, new ConcurrentLinkedQueue()).add(event)

        event.status match {
          case Status.Failure | Status.Error => currentResult.failures.add(task.taskDef.fullyQualifiedName)
          case _                             =>
        }
      },
      Array(new PrefixedTestingLogger(logger, "    ")),
    )

    additionalTasks.foreach(submitTask)
    reporter.postTask()
  }

  override def waitForTasks(): Future[TaskExecutorResult] = {
    val result = currentResult.toTaskExecutorResult

    currentResult.clear()

    Future.successful(result)
  }
}
