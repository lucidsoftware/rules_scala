package higherkindness.rules_scala
package common.interrupt

object InterruptUtil {
  def throwIfInterrupted(): Unit = {
    if (Thread.interrupted()) {
      throw new InterruptedException("WorkRequest was cancelled.")
    }
  }
}
