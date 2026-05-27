package higherkindness.rules_scala.common.sbt_testing

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import sbt.testing.Logger

private sealed trait SbtLogEntry
private object SbtLogEntry {
  case class Error(message: String) extends SbtLogEntry
  case class Warn(message: String) extends SbtLogEntry
  case class Info(message: String) extends SbtLogEntry
  case class Debug(message: String) extends SbtLogEntry
  case class Trace(throwable: Throwable) extends SbtLogEntry
}

class BufferedLogger(underlying: Logger) extends Logger {
  private val buffer = new ConcurrentLinkedQueue[SbtLogEntry]()
  private val flushed = new AtomicBoolean(false)

  private def addEntry(entry: SbtLogEntry): Unit = if (flushed.get()) {
    writeEntry(entry, late = true)
  } else {
    buffer.add(entry)
  }

  private def writeEntry(entry: SbtLogEntry, late: Boolean = false): Unit = {
    // Sometimes, tests may log after they complete. We should handle this gracefully, so add a "[late]" tag to any
    // messages that come in after `flush` is called to make them easier to identify.
    val tag = if (late) "[late] " else ""

    entry match {
      case SbtLogEntry.Error(message)   => underlying.error(tag + message)
      case SbtLogEntry.Warn(message)    => underlying.warn(tag + message)
      case SbtLogEntry.Info(message)    => underlying.info(tag + message)
      case SbtLogEntry.Debug(message)   => underlying.debug(tag + message)
      case SbtLogEntry.Trace(throwable) => underlying.trace(throwable)
    }
  }

  override def ansiCodesSupported(): Boolean = underlying.ansiCodesSupported()
  override def error(message: String): Unit = addEntry(SbtLogEntry.Error(message))
  override def warn(message: String): Unit = addEntry(SbtLogEntry.Warn(message))
  override def info(message: String): Unit = addEntry(SbtLogEntry.Info(message))
  override def debug(message: String): Unit = addEntry(SbtLogEntry.Debug(message))
  override def trace(throwable: Throwable): Unit = addEntry(SbtLogEntry.Trace(throwable))

  def flush(): Unit = {
    flushed.set(true)

    var entry = buffer.poll()

    while (entry != null) {
      writeEntry(entry)

      entry = buffer.poll()
    }
  }
}
