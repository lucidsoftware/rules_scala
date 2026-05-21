package higherkindness.rules_scala.common.sbt_testing

import sbt.testing.{Event, Logger, Status, Task}
import scala.collection.mutable

class TestTaskExecutor(logger: Logger) {
  def execute(task: Task, failures: mutable.Set[String]): mutable.ListBuffer[Event] = {
    var events = new mutable.ListBuffer[Event]()
    def execute(task: Task): Unit = {
      val tasks = task.execute(
        event => {
          events += event
          event.status match {
            case Status.Failure | Status.Error =>
              failures += task.taskDef.fullyQualifiedName
            case _ =>
          }
        },
        Array(new PrefixedTestingLogger(logger, "    ")),
      )
      tasks.foreach(execute)
    }
    execute(task)
    events
  }
}
