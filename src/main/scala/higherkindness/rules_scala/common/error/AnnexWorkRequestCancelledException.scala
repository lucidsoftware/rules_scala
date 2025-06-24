package higherkindness.rules_scala.common.error

class AnnexDuplicateActiveRequestException(
  val message: String = "",
  val cause: Throwable = null,
) extends Exception(message, cause)
