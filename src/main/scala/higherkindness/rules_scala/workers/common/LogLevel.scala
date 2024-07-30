package higherkindness.rules_scala
package workers.common

sealed abstract class LogLevel(val level: String)
object LogLevel {
  case object Error extends LogLevel("error")
  case object Warn extends LogLevel("warn")
  case object Info extends LogLevel("info")
  case object Debug extends LogLevel("debug")
  case object None extends LogLevel("none")
  val values = Map(
    Error.level -> Error,
    Warn.level -> Warn,
    Info.level -> Info,
    Debug.level -> Debug,
    None.level -> None,
  )

  def apply(level: String): LogLevel = values(level)
}
