package higherkindness.rules_scala
package common.worker

import java.util.concurrent.{Callable, CancellationException, FutureTask, TimeUnit}
import java.util.concurrent.locks.ReentrantLock

/**
 * This is a FutureTask that, when cancelled, waits for its callable to end, either by interruption or by completing.
 *
 * The regular FutureTask is immediately marked done when you cancel it, regardless of the status of the callable. That
 * becomes a problem for if the worker receives many work requests, has them cancelled, and then immediately receives
 * more work requests. The callable could still be running, but we've received more work to do.
 *
 * That can create funky book keeping situations for Bazel: imagine you are running compile actions that take 60 seconds
 * and can't be interrupted. Bazel asks you to cancel them 30 seconds after they start. You respond to Bazel saying
 * they've been cancelled. Bazel sends you more work requests that also take 60 seconds to compile. You don't have any
 * threads to run them as they're all still finishing hte original requests. As a result it looks like these new compile
 * actions take 90 seconds because they had to wait 30 seconds to get threads in order to start executing.
 *
 * This class was heavily inspired by the following:
 * https://stackoverflow.com/questions/6040962/wait-for-cancel-on-futuretask?rq=3
 */
class FutureTaskWaitOnCancel[S](
  callable: CallableLockedWhileRunning[S],
) extends FutureTask[S](callable) {

  def this(callable: Callable[S]) = {
    this(new CallableLockedWhileRunning[S](callable))
  }

  private def waitForCallable(): Unit = {
    // If the callable is running, wait for it to complete or be interrupted
    callable.isRunning.lock()
    callable.isRunning.unlock()
  }

  override def get(): S = {
    try {
      super.get()
    } catch {
      case e: CancellationException =>
        waitForCallable()
        throw e
    }
  }

  override def get(timeout: Long, unit: TimeUnit): S = {
    throw new UnsupportedOperationException()
  }

  override def cancel(mayInterruptIfRunning: Boolean): Boolean = {
    val result = super.cancel(mayInterruptIfRunning)
    waitForCallable()
    result
  }
}

private class CallableLockedWhileRunning[S](callable: Callable[S]) extends Callable[S] {
  private[worker] val isRunning = new ReentrantLock()

  override def call(): S = {
    isRunning.lock()
    try {
      callable.call()
    } finally {
      isRunning.unlock()
    }
  }
}
