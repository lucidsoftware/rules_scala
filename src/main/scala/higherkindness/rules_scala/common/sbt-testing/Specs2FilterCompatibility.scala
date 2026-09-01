package higherkindness.rules_scala.common.sbt_testing

private[sbt_testing] object Specs2FilterCompatibility {
  private val QuoteStart = raw"\Q"
  private val QuoteEnd = raw"\E"

  /**
   * IntelliJ's Scala plugin replaces parentheses with square brackets in Bazel Specs2 filters. We accept either form
   * inside quoted regex sections, so the transformed filter still selects the intended example name without changing
   * the meaning of unquoted regex syntax.
   */
  def normalize(filter: String): String = {
    val result = new StringBuilder
    var index = 0
    var quoted = false

    while (index < filter.length) {
      if (!quoted && filter.startsWith(QuoteStart, index)) {
        result.append(QuoteStart)
        index += QuoteStart.length
        quoted = true
      } else if (quoted && filter.startsWith(QuoteEnd, index)) {
        result.append(QuoteEnd)
        index += QuoteEnd.length
        quoted = false
      } else {
        filter.charAt(index) match {
          case '[' if quoted => result.append(raw"\E(?:\[|\()\Q")
          case ']' if quoted => result.append(raw"\E(?:\]|\))\Q")
          case character     => result.append(character)
        }
        index += 1
      }
    }

    result.toString
  }
}
