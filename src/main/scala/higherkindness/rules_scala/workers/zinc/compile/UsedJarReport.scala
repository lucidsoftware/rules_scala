package higherkindness.rules_scala.workers.zinc.compile

import higherkindness.rules_scala.workers.common.{AnnexMapper, FileUtil}
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters.*
import xsbti.compile.analysis.WriteMapper

/**
 * Normalizes the JAR paths outputted by
 * [[higherkindness.rules_scala.workers.zinc.compile.usedjarreporter.AnnexUsedJarReporterPlugin]].
 *
 * This needs to be done in a separate step because that compiler plugin doesn't have access to the metadata we need for
 * normalization (namely, the short paths of the compile classpath entries).
 */
object UsedJarReport {
  def normalize(output: Path, classpath: List[Path], exportedJars: List[String], writeMapper: WriteMapper): Unit = {
    val compileJars = classpath.map(_.toAbsolutePath.normalize()).toSet
    val used = Files
      .readAllLines(output)
      .asScala
      .map(path => Paths.get(path).toAbsolutePath.normalize())
      .filter(compileJars.contains)
      .map { path =>
        val mapped = writeMapper.mapClasspathEntry(path)
        val relative = if (mapped.startsWith(AnnexMapper.rootPlaceholder)) {
          AnnexMapper.rootPlaceholder.relativize(mapped)
        } else {
          mapped
        }

        FileUtil.bazelShortPath(relative).toString
      }
      .toSet

    // Exported JARs are automatically included on the transitive compile classpath of the current build target
    // (this is a feature of `JavaInfo`). We need to include them in the list of used JARs so they're available to
    // targets that depend on this one.
    val retained = (used ++ exportedJars).toList.sorted

    Files.write(output, retained.asJava)
  }

  def reset(output: Path): Unit = Files.deleteIfExists(output)
}
