package sbt.internal.inc

import xsbti.compile.{CompileOptions, MiniSetup}
import java.nio.file.{Path, Paths}

/**
 * Zinc's MiniSetup contains scalacOptions which include the -sourceroot flag that references absolute sandbox paths.
 * These paths are non-deterministic across builds because the sandbox directory changes (e.g., __sandbox/4/_main vs
 * __sandbox/8/_main), making the analysis files non-deterministic.
 *
 * This class filters out the -sourceroot option from the scalacOptions to ensure deterministic analysis files.
 *
 * TODO: Consider if there's a better way to handle this upstream in Zinc
 */

object FilteredSetup {
  private val sourcerootFlag = "-sourceroot"

  def getFilteredSetup(setup: MiniSetup): MiniSetup = {
    val options = setup.options()
    // Filter out the -sourceroot option and its value
    val filteredScalacOptions = {
      val originalOptions = options.scalacOptions()
      val filtered = scala.collection.mutable.ArrayBuffer[String]()
      var i = 0
      while (i < originalOptions.length) {
        val option = originalOptions(i)
        if (option == sourcerootFlag) {
          // Skip both the flag and its value (next argument)
          i += 2
        } else {
          filtered += option
          i += 1
        }
      }
      filtered.toArray
    }

    // Create new CompileOptions with filtered scalac options
    val newOptions = options.withScalacOptions(filteredScalacOptions)
    // Create new MiniSetup with filtered options
    setup.withOptions(newOptions)
  }
}
