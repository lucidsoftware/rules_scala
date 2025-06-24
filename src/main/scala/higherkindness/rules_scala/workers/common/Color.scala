package higherkindness.rules_scala.workers.common

import scala.Console.{GREEN as CG, RED as CR, RESET as CRESET, YELLOW as CY}

object Color {
  def Info(message: String): String = colorString(message, CG, "Info")
  def Warning(message: String): String = colorString(message, CY, "Warn")
  def Error(message: String): String = colorString(message, CR, "Error")

  private def colorString(message: String, color: String, severity: String): String = {
    val header = s"[$color$severity$CRESET]"
    s"$header " + message.replaceAll("\n", s"\n$header ")
  }
}
