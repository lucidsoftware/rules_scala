package higherkindness.rules_scala
package workers.common

import java.io.File
import java.nio.file.{Path, Paths}
import sbt.internal.inc.Analysis
import xsbti.compile.AnalysisStore
import xsbti.compile.analysis.ReadWriteMappers

object AnalysisUtil {

  def getAnalysisStore(
    analysisStoreFile: File,
    debug: Boolean,
    isIncremental: Boolean,
    workDir: Path,
  ): AnalysisStore = {
    val readWriteMappers = AnnexMapper.mappers(workDir, isIncremental)
    getAnalysisStore(analysisStoreFile, debug, readWriteMappers)
  }

  def getAnalysisStore(
    analysisStoreFile: File,
    debug: Boolean,
    readWriteMappers: ReadWriteMappers,
  ): AnalysisStore = {
    if (debug) {
      ConsistentFileAnalysisStore.text(
        analysisStoreFile,
        readWriteMappers,
      )
    } else {
      ConsistentFileAnalysisStore.binary(
        analysisStoreFile,
        readWriteMappers,
        reproducible = true,
      )
    }
  }

  def getAnalysis(analysisStore: AnalysisStore): Analysis = {
    analysisStore.get().get().getAnalysis.asInstanceOf[Analysis]
  }
}
