package higherkindness.rules_scala
package common.error

final class AnnexWorkerError(
  val code: Int,
  val message: String = "",
  val cause: Throwable = null,
) extends Error(message, cause)

object AnnexWorkerError {
  def unapply(e: AnnexWorkerError): Option[(Int, String, Throwable)] = Some((e.code, e.getMessage, e.getCause))
}
