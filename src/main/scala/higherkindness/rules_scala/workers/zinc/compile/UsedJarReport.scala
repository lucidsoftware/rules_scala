package higherkindness.rules_scala.workers.zinc.compile

import com.google.devtools.build.lib.view.proto.Deps
import higherkindness.rules_scala.workers.common.{AnnexMapper, FileUtil}
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters.*
import xsbti.compile.analysis.WriteMapper

/**
 * Merges and normalizes the JAR paths reported by JavaBuilder and
 * [[higherkindness.rules_scala.workers.zinc.compile.usedjarreporter.AnnexUsedJarReporterPlugin]].
 *
 * This needs to be done in a separate step because that compiler plugin doesn't have access to the metadata we need for
 * normalization (namely, the short paths of the compile classpath entries).
 */
object UsedJarReport {
  def normalize(
    output: Path,
    classpath: List[Path],
    exportedJars: List[String],
    writeMapper: WriteMapper,
    javaDeps: Option[Path],
  ): Unit = {
    val compileJars = classpath.map(_.toAbsolutePath.normalize()).toSet

    def shortPath(path: Path): String = {
      val mapped = writeMapper.mapClasspathEntry(path)
      val relative = if (mapped.startsWith(AnnexMapper.rootPlaceholder)) {
        AnnexMapper.rootPlaceholder.relativize(mapped)
      } else {
        mapped
      }

      FileUtil.bazelShortPath(relative).toString
    }

    val scalaUsed = Files.readAllLines(output).asScala.map(path => Paths.get(path).toAbsolutePath.normalize())
    val javaUsed = javaDeps.toList.flatMap { path =>
      val report = Deps.Dependencies.parseFrom(Files.readAllBytes(path))

      require(report.getSuccess, s"JavaBuilder did not produce a successful dependency report: $path")

      val dependencies = report.getDependencyList.asScala.filter { dependency =>
        compileJars.contains(Paths.get(dependency.getPath).toAbsolutePath.normalize())
      }

      dependencies
        .filterNot(_.getKind == Deps.Dependency.Kind.UNUSED)
        .map(dependency => Paths.get(dependency.getPath).toAbsolutePath.normalize())
    }

    val used = (scalaUsed ++ javaUsed)
      .filter(compileJars.contains)
      .map(shortPath)
      .toSet

    // Exported JARs are automatically included on the transitive compile classpath of the current build target
    // (this is a feature of `JavaInfo`). We need to include them in the list of used JARs so they're available to
    // targets that depend on this one.
    val retained = (used ++ exportedJars).toList.sorted

    Files.write(output, retained.asJava)
  }

  def reset(output: Path): Unit = Files.deleteIfExists(output)
}
