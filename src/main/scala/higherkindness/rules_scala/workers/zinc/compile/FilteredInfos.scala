package sbt.internal.inc

import sbt.internal.inc.SourceInfos
import xsbti.compile.analysis.SourceInfo
import xsbti.{Problem, VirtualFileRef}

/**
 * The consistent analysis format in Zinc does not send the Position in the Problems in the SourceInfos through the read
 * or write mapper, so the file paths are absolute rather than relativized, which means they are not machine independent
 * and reproducible.
 *
 * This filters out the paths from those Problems/Positions, so we can get a deterministic output.
 *
 * TODO: fix this problem in Zinc.
 */
object FilteredInfos {
  def getFilteredInfos(infos: SourceInfos): SourceInfos = {

    val filteredInfos = infos.allInfos.map { case (virtualFileRef: VirtualFileRef, info: SourceInfo) =>
      val underlyingInfo = info.asInstanceOf[UnderlyingSourceInfo]

      val filteredInfo = SourceInfos.makeInfo(
        reported = Seq.empty[Problem],
        unreported = Seq.empty[Problem],
        mainClasses = underlyingInfo.mainClasses,
      )
      (virtualFileRef, filteredInfo)
    }

    SourceInfos.of(filteredInfos)
  }
}
