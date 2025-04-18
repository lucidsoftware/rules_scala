package higherkindness.rules_scala
package common.interrupt

import java.util.concurrent.CancellationException

object InterruptUtil {
  def throwIfInterrupted(isCancelled: Function0[Boolean]): Unit = {
    if (Thread.interrupted()) {
      throw new InterruptedException("WorkRequest was cancelled via Thread interruption.")
    } else if (isCancelled()) {
      throw new CancellationException("WorkRequest was cancelled via FutureTask cancellation.")
    }
  }
}
