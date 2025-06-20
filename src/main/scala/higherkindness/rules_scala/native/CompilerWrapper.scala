package higherkindness.rules_scala.native

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters.*

object CompilerWrapper {
  private def expandArgumentFile(arguments: Array[String]): Array[String] =
    // We assume that the argument file isn't mixed with other arguments
    if (arguments.nonEmpty && arguments(0).startsWith("@")) {
      val argumentFileArgument = arguments(0)
      val argumentFilePath = Paths.get(argumentFileArgument.slice(1, argumentFileArgument.length))

      Files.lines(argumentFilePath, StandardCharsets.UTF_8).iterator.asScala.toArray
    } else {
      arguments
    }

  def main(arguments: Array[String]): Unit = scala.tools.nsc.Main.main(expandArgumentFile(arguments))
}
