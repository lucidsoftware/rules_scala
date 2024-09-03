package higherkindness.rules_scala
package common.worker

import java.util.concurrent.{Callable, FutureTask}
import scala.concurrent.{ExecutionContext, ExecutionException, Future, Promise}
import scala.util.Try

/**
 * This is more or less a cancellable Future. It stitches together Scala Future, which is not cancellable, with the Java
 * FutureTask, which is cancellable.
 *
 * However, it uses our extension on FutureTask, which, upon cancellation, waits for the callable to be interrupted or
 * complete. That way we can be confident the task is no longer running when we respond to Bazel that it has been
 * cancelled.
 *
 * Heavily inspired by the following: https://github.com/NthPortal/cancellable-task/tree/master
 * https://stackoverflow.com/a/39986418/6442597
 */
class CancellableTask[S] private (fn: => S) {
  private val promise = Promise[S]()
  val future: Future[S] = promise.future

  private val fnCallable = new Callable[S]() {
    def call(): S = fn
  }

  private val task = new FutureTaskWaitOnCancel[S](fnCallable) {
    override def done() = promise.complete {
      Try(get()).recover {
        // FutureTask wraps exceptions in an ExecutionException. We want to re-throw the underlying
        // error because Scala's Future handles things like fatal exception in a special way that
        // we miss out on if they're wrapped in that ExecutionException. Put another way: leaving
        // them wrapped in the ExecutionException breaks the contract that Scala Future users expect.
        case e: ExecutionException => throw e.getCause()
      }
    }
  }

  def cancel(mayInterruptIfRunning: Boolean): Boolean = task.cancel(mayInterruptIfRunning)

  def execute(executionContext: ExecutionContext): Unit = executionContext.execute(task)
}

object CancellableTask {
  def apply[S](fn: => S): CancellableTask[S] = {
    new CancellableTask(fn)
  }
}
