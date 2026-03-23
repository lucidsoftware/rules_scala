package higherkindness.rules_scala.common.worker

import java.util.concurrent.Callable
import scala.concurrent.{ExecutionContext, Future, Promise}
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
 *
 * Note that for complicated reasons explained in its implementation, `CancellableTask` wraps all exceptions thrown
 * within the task in an `ExecutionException`, so be sure to unwrap them.
 */
class CancellableTask[S] private (fn: Function1[Function0[Boolean], S]) {
  private val promise = Promise[S]()
  val future: Future[S] = promise.future

  private val fnCallable = new Callable[S]() {
    def call(): S = fn(isCancelled)
  }

  private val task = new FutureTaskWaitOnCancel[S](fnCallable) {
    override def done() = promise.complete {
      // `FutureTask` wraps exceptions in an `ExecutionException`. Although we'd like `FutureTask` to function exactly
      // like a `Future` (which doesn't wrap exceptions like this), we can't unwrap fatal exceptions. That's because
      // `promise.complete` will just re-wrap fatal exceptions in an `ExecutionException`. To be consistent about how we
      // handle various exceptions, we leave all exceptions unwrapped and declare it the responsibility of the user to
      // unwrap the exceptions they wish to handle.
      Try(get())
    }
  }

  def cancel(mayInterruptIfRunning: Boolean): Boolean = task.cancel(mayInterruptIfRunning)

  def execute(executionContext: ExecutionContext): Unit = executionContext.execute(task)

  def isCancelled(): Boolean = task.isCancelled()
}

object CancellableTask {
  def apply[S](fn: => S): CancellableTask[S] = {
    new CancellableTask((_: Function0[Boolean]) => fn)
  }

  def apply[S](fn: Function1[Function0[Boolean], S]): CancellableTask[S] = {
    new CancellableTask(fn)
  }
}
