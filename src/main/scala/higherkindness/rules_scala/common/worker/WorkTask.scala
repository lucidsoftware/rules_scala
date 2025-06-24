package higherkindness.rules_scala.common.worker

import java.io.PrintStream
import java.nio.file.Path

case class WorkTask[S](
  context: S,
  args: Array[String],
  output: PrintStream,
  workDir: Path,
  verbosity: Int,
  isCancelled: Function0[Boolean],
)
