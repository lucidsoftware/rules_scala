package higherkindness.rules_scala
package workers.common

import xsbti.{Logger, Problem}
import sbt.internal.inc.{LoggedReporter => SbtLoggedReporter, ProblemStringFormats}

class LoggedReporter(logger: Logger, versionString: String) extends SbtLoggedReporter(0, logger) {
  private val problemStringFormats = new ProblemStringFormats {}

  // Scala 3 has great error messages, let's leave those alone, but still add color to the Scala 2 messages
  private val shouldEnhanceMessage = !versionString.startsWith("0.") && !versionString.startsWith("3")

  private def doLog(problem: Problem, colorFunction: String => String): String = {
    val formattedProblem = problemStringFormats.ProblemStringFormat.showLines(problem).mkString("\n")

    if (shouldEnhanceMessage) colorFunction(formattedProblem) else formattedProblem
  }

  override protected def logError(problem: Problem): Unit = {
    logger.error(() => doLog(problem, Color.Error))
  }
  override protected def logInfo(problem: Problem): Unit = {
    logger.info(() => doLog(problem, Color.Info))
  }
  override protected def logWarning(problem: Problem): Unit = {
    logger.warn(() => doLog(problem, Color.Warning))
  }
}
