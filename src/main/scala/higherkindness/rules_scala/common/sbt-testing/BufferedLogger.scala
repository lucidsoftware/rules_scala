package higherkindness.rules_scala.common.sbt_testing

import sbt.testing.Logger

import scala.collection.mutable

private sealed trait SbtLogEntry
private object SbtLogEntry {
  case class Error(message: String) extends SbtLogEntry
  case class Warn(message: String) extends SbtLogEntry
  case class Info(message: String) extends SbtLogEntry
  case class Debug(message: String) extends SbtLogEntry
  case class Trace(throwable: Throwable) extends SbtLogEntry
}

class BufferedLogger(underlying: Logger) extends Logger {
  private val buffer = mutable.ArrayBuffer.empty[SbtLogEntry]

  override def ansiCodesSupported(): Boolean = underlying.ansiCodesSupported()
  override def error(message: String): Unit = buffer.addOne(SbtLogEntry.Error(message))
  override def warn(message: String): Unit = buffer.addOne(SbtLogEntry.Warn(message))
  override def info(message: String): Unit = buffer.addOne(SbtLogEntry.Info(message))
  override def debug(message: String): Unit = buffer.addOne(SbtLogEntry.Debug(message))
  override def trace(throwable: Throwable): Unit = buffer.addOne(SbtLogEntry.Trace(throwable))

  def flush(): Unit = {
    buffer.foreach {
      case SbtLogEntry.Error(message)   => underlying.error(message)
      case SbtLogEntry.Warn(message)    => underlying.warn(message)
      case SbtLogEntry.Info(message)    => underlying.info(message)
      case SbtLogEntry.Debug(message)   => underlying.debug(message)
      case SbtLogEntry.Trace(throwable) => underlying.trace(throwable)
    }

    buffer.clear()
  }
}
