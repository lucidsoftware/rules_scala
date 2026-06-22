package higherkindness.rules_scala.common.sandbox

import java.io.File
import java.nio.file.{Path, Paths}
import scala.jdk.CollectionConverters.*

/**
 * A [[PathResolver]] provides methods to resolve paths in persistent workers and binary/test rule runners.
 *
 * Don't manually instantiate it; use one of the factory methods in the companion object instead.
 */
trait PathResolver {
  def resolve(path: Path): Path

  final def resolve(file: File): File = resolve(file.toPath).toFile
  final def resolve[A <: Iterable[Path]](paths: A): List[Path] = paths.view.map(resolve).toList
  final def resolve(paths: java.util.List[Path]): List[Path] = resolve(paths.asScala)
}

object PathResolver {
  private[sandbox] def forBinaryRunner(workingDirectory: Option[String], runPath: String): PathResolver = {
    // `System.getProperty("user.dir")` returns the physical working directory, not the logical one
    val logicalWorkingDirectory = workingDirectory.map(Paths.get(_)).getOrElse(Paths.get("").toAbsolutePath)
    val logicalRunPath = logicalWorkingDirectory.resolve(runPath)

    // Normalizing the resolved path is necessary because if `path` begins with `..`, the JVM will look inside the
    // parent directory of the physical run path, not the logical run path
    logicalRunPath.resolve(_).normalize()
  }

  /**
   * Since [[higherkindness.rules_scala.workers.zinc.test.TestRunner]] and
   * [[higherkindness.rules_scala.workers.zinc.repl.ReplRunner]] both run in binary/test rules, they need to access
   * runfiles. Normally, these can be found relative to the current directory, but in some remote execution environments
   * (namely, those that symlink action inputs), the working directory is a symlink.
   *
   * In these environments, path resolution should be performed _before_ resolving those symlinks (i.e. against the
   * logical working directory, not the physical one). This [[PathResolver]] performs that resolution.
   */
  def forBinaryRunner: PathResolver = forBinaryRunner(sys.env.get("PWD"), sys.props("bazel.runPath"))

  /**
   * When a persistent worker processes a work request, it's given a "work directory" (`workDir`) that all other paths
   * (`path`) should be resolved relative to. This [[PathResolver]] allows persistent workers to resolve these paths.
   *
   * This is only relevant for sandboxed multiplexed workers; they're given a non-empty work directory because they
   * handle multiple work requests concurrently in the same process, each in isolation. All other types of persistent
   * workers will receive an empty work directory, in which case this resolver won't alter `path`.
   */
  def forPersistentWorker(workDir: Path): PathResolver = workDir.resolve(_)
}
