package higherkindness.rules_scala
package workers.zinc.compile

import workers.common.FileUtil

import java.nio.file.Files
import java.nio.file.Path

trait ZincPersistence {
  def load(): Unit
  def save(): Unit
}

class FilePersistence(cacheDir: Path, analysisStorePath: Path, jar: Path) extends ZincPersistence {
  private[this] val cacheAnalysisStorePath: Path = cacheDir.resolve("analysis_store.gz")
  private[this] val cacheJar: Path = cacheDir.resolve("classes.jar")

  /**
   * Existence indicates that files are incomplete.
   */
  private[this] val tmpMarker: Path = cacheDir.resolve(".tmp")

  def load(): Unit = {
    if (Files.exists(cacheDir) && Files.notExists(tmpMarker)) {
      Files.copy(cacheAnalysisStorePath, analysisStorePath)
      Files.copy(cacheJar, jar)
    }
  }
  def save(): Unit = {
    if (Files.exists(cacheDir)) {
      FileUtil.delete(cacheDir)
    }
    Files.createDirectories(cacheDir)
    Files.createFile(tmpMarker)
    Files.copy(analysisStorePath, cacheAnalysisStorePath)
    Files.copy(jar, cacheJar)
    Files.delete(tmpMarker)
  }
}

object NullPersistence extends ZincPersistence {
  def load(): Unit = ()
  def save(): Unit = ()
}
