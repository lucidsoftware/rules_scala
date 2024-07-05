package higherkindness.rules_scala
package workers.common

import java.io.File
import java.nio.file.Paths
import sbt.internal.inc.Analysis
import sbt.internal.inc.consistent.ConsistentFileAnalysisStore
import xsbti.compile.AnalysisStore
import xsbti.compile.analysis.ReadWriteMappers

object AnalysisUtil {
  def getAnalysisStore(analysisStoreFile: File, debug: Boolean, isIncremental: Boolean): AnalysisStore = {
    val readWriteMappers = AnnexMapper.mappers(Paths.get(""), isIncremental)

    if (debug) {
      ConsistentFileAnalysisStore.text(
        analysisStoreFile,
        readWriteMappers,
        sort = true,
      )
    } else {
      ConsistentFileAnalysisStore.binary(
        analysisStoreFile,
        readWriteMappers,
        sort = true,
      )
    }
  }

  def getAnalysis(analysisStore: AnalysisStore): Analysis = {
    analysisStore.get().get().getAnalysis.asInstanceOf[Analysis]
  }
}
