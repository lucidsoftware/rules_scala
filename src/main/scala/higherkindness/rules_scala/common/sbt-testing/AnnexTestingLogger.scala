package higherkindness.rules_scala
package common.sbt_testing

import sbt.testing.Logger

final class AnnexTestingLogger(color: Boolean, verbosity: Verbosity) extends Logger with Serializable {
  def ansiCodesSupported = color

  def error(msg: String) = println(s"$msg")

  def warn(msg: String) = println(s"$msg")

  def info(msg: String) = verbosity match {
    case Verbosity.High | Verbosity.Medium => println(s"$msg")
    case _                                 =>
  }

  def debug(msg: String) = verbosity match {
    case Verbosity.High => println(s"$msg")
    case _              =>
  }

  def trace(err: Throwable) = println(s"${err.getMessage}")
}
