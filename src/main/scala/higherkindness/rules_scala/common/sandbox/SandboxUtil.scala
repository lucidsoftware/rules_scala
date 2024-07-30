package higherkindness.rules_scala
package common.sandbox

import java.io.File
import java.nio.file.Path
import java.util.List as JList
import scala.collection.mutable.Buffer
import scala.jdk.CollectionConverters._

object SandboxUtil {

  /**
   * Returns a function, that when given a path, returns a path prefixed by the workDir. This is only relevant for
   * sandboxed multiplexed workers - they will have a workDir prefix. All other prefixes will empty, so it will just use
   * the given.
   *
   * This is a convenience function for workers to easily get functions to prefix their path and file operations
   */
  def getSandboxPath(workDir: Path, path: Path): Path = {
    workDir.resolve(path)
  }
  def getSandboxFile(workDir: Path, file: File): File = {
    workDir.resolve(file.toPath).toFile
  }
  def getSandboxPaths(workDir: Path, paths: JList[Path]): List[Path] = {
    getSandboxPaths(workDir, paths.asScala)
  }
  def getSandboxPaths(workDir: Path, paths: Buffer[Path]): List[Path] = {
    paths.view.map(getSandboxPath(workDir, _)).toList
  }
}
