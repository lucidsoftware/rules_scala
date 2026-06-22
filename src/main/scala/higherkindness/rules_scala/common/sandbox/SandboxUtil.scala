package higherkindness.rules_scala.common.sandbox

import java.io.File
import java.nio.file.Path
import java.util.List as JList
import scala.collection.mutable.Buffer

object SandboxUtil {
  def getSandboxPath(workDir: Path, path: Path): Path = PathResolver.forPersistentWorker(workDir).resolve(path)
  def getSandboxFile(workDir: Path, file: File): File = PathResolver.forPersistentWorker(workDir).resolve(file)
  def getSandboxPaths(workDir: Path, paths: JList[Path]): List[Path] =
    PathResolver.forPersistentWorker(workDir).resolve(paths)

  def getSandboxPaths(workDir: Path, paths: Buffer[Path]): List[Path] =
    PathResolver.forPersistentWorker(workDir).resolve(paths)
}
