package higherkindness.rules_scala
package common.error

import java.io.PrintStream

/**
 * Represents an error that occurs in a work request.
 * @param code
 *   The exit code sent back to Bazel for this work request
 * @param message
 *   The error message.
 * @param cause
 *   The cause of this error.
 */
class AnnexWorkerError(
  val code: Int,
  val message: String = "",
  val cause: Throwable = null,
) extends Error(message, cause) {

  /**
   * AnnexWorkerError is a bit multipurpose, so we only print the full stack trace if there's an exeption set as the
   * cause. Otherwise we assume the error is just an error message.
   */
  def print(out: PrintStream): Unit = {
    if (cause != null) {
      this.printStackTrace(out)
    } else if (!message.isEmpty) {
      out.print(message)
    }
  }
}

object AnnexWorkerError {
  def unapply(e: AnnexWorkerError): Option[(Int, String, Throwable)] = {
    Some((e.code, e.getMessage, e.getCause))
  }
}
