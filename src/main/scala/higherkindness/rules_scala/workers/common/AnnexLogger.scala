package higherkindness.rules_scala.workers.common

import java.io.{PrintStream, PrintWriter, StringWriter}
import java.nio.file.Path
import java.util.function.Supplier
import xsbti.Logger

final class AnnexLogger(level: LogLevel, workDir: Path, out: PrintStream) extends Logger {

  private val root = s"${workDir.toAbsolutePath().normalize()}/"

  private def format(value: String): String = value.replace(root, "")

  def debug(msg: Supplier[String]): Unit = level match {
    case LogLevel.Debug => out.println(format(msg.get))
    case _              =>
  }

  def error(msg: Supplier[String]): Unit = level match {
    case LogLevel.Error | LogLevel.Warn | LogLevel.Info | LogLevel.Debug => out.println(format(msg.get))
    case _                                                               =>
  }

  def info(msg: Supplier[String]): Unit = level match {
    case LogLevel.Info | LogLevel.Debug => out.println(format(msg.get))
    case _                              =>
  }

  def trace(err: Supplier[Throwable]): Unit = level match {
    case LogLevel.Error | LogLevel.Warn | LogLevel.Info | LogLevel.Debug =>
      val trace = new StringWriter();
      err.get.printStackTrace(new PrintWriter(trace));
      out.println(format(trace.toString))
    case _ =>
  }

  def warn(msg: Supplier[String]): Unit = level match {
    case LogLevel.Warn | LogLevel.Info | LogLevel.Debug => out.println(format(msg.get))
    case _                                              =>
  }

}
