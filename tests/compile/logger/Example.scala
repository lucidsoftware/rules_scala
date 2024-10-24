import higherkindness.rules_scala.workers.common.AnnexLogger
import higherkindness.rules_scala.workers.common.Color
import higherkindness.rules_scala.workers.common.LogLevel
import higherkindness.rules_scala.workers.common.LoggedReporter

import java.nio.file.Paths
import sbt.internal.inc.javac.JavaPosition
import sbt.util.InterfaceUtil.problem
import xsbti.Severity

object Example {
    def main(args: Array[String]) {
        val logger = new AnnexLogger(LogLevel.Info, Paths.get(""), System.err)
        val reporter = new LoggedReporter(logger, "2.13.14")
        val problem1 = problem("", new JavaPosition("Test Line", 100, "", 99, ""), "Info Message 1", Severity.Info)
        val problem2 = problem("", new JavaPosition("Test Line", 200, "", 199, ""), "Warning Message 2", Severity.Warn)
        val problem3 = problem("", new JavaPosition("Test Line", 300, "", 299, ""), "Error Message 3", Severity.Error)
        // Test logger
        reporter.log(problem1)
        reporter.log(problem2)
        reporter.log(problem3)
        // Test Color object
        System.err.println(Color.Info("This is an info"))
        System.err.println(Color.Warning("This is a warning"))
        System.err.println(Color.Error("This is an error"))
    }
}
