package higherkindness.rules_scala
package workers.common

import java.io.File
import sbt.internal.inc.Analysis
import sbt.internal.inc.consistent.ConsistentFileAnalysisStore
import xsbti.compile.AnalysisStore
import xsbti.compile.analysis.ReadWriteMappers

object AnalysisUtil {
  def getAnalysisStore(analysisStoreFile: File, debug: Boolean): AnalysisStore = {
    if (debug) {
      ConsistentFileAnalysisStore.text(
        analysisStoreFile,
        ReadWriteMappers.getEmptyMappers,
        sort = true,
      )
    } else {
      ConsistentFileAnalysisStore.binary(
        analysisStoreFile,
        ReadWriteMappers.getEmptyMappers,
        sort = true,
      )
    }
  }

  def getAnalysis(analysisStore: AnalysisStore): Analysis = {
    analysisStore.get().get().getAnalysis.asInstanceOf[Analysis]
  }
}
