package higherkindness.rules_scala
package common.args

import common.error.AnnexWorkerError
import java.io.{PrintStream, PrintWriter}
import java.nio.file.{Path, Paths}
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.inf.{Argument, ArgumentParser, ArgumentParserException, ArgumentType, Namespace}
import scala.language.reflectiveCalls

object ArgsUtil {

  /**
   * Safely handles expected errors that pop up during parsing arguments. The argparse4j calls System.exit when things
   * go wrong, and we don't want the worker to exit. Instead we exit the work request and print to the output stream.
   */
  def parseArgsOrFailSafe(args: Array[String], parser: ArgumentParser, out: PrintStream): Namespace = {
    try {
      parser.parseArgs(args)
    } catch {
      case e: HelpScreenException =>
        parser.handleError(e, new PrintWriter(out))
        throw new AnnexWorkerError(0)
      case e: ArgumentParserException =>
        parser.handleError(e, new PrintWriter(out))
        throw new AnnexWorkerError(1)
    }
  }

  case class PathArgumentType() extends ArgumentType[Path] {
    override def convert(parser: ArgumentParser, arg: Argument, value: String): Path = {
      Paths.get(value)
    }
  }
}
