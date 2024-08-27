package higherkindness.rules_scala
package common.sbt_testing

sealed abstract class Verbosity(val level: String)
object Verbosity {
  case object High extends Verbosity("HIGH")
  case object Medium extends Verbosity("MEDIUM")
  case object Low extends Verbosity("LOW")
  val values = Map(
    High.level -> High,
    Medium.level -> Medium,
    Low.level -> Low,
  )

  def apply(level: String): Verbosity = values(level)
}
