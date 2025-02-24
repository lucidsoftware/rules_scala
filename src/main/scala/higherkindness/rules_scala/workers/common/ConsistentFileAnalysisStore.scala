/*
 * Zinc - The incremental compiler for Scala.
 * Copyright Scala Center, Lightbend, and Mark Harrah
 *
 * Licensed under Apache License 2.0
 * SPDX-License-Identifier: Apache-2.0
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package higherkindness.rules_scala
package workers.common

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.Files
import java.util.Optional
import sbt.internal.inc.consistent.{ConsistentAnalysisFormat, Deserializer, ParallelGzipOutputStream, Serializer, SerializerFactory}
import sbt.io.{IO, Using}
import scala.jdk.OptionConverters.*
import scala.util.control.Exception.allCatch
import xsbti.compile.analysis.ReadWriteMappers
import xsbti.compile.{AnalysisContents, AnalysisStore => XAnalysisStore}

/**
 * This is a modified version of Zinc's [[ConsistentFileAnalysisStore]], which you can view here:
 * [[https://github.com/sbt/zinc/blob/1.10.x/internal/zinc-persist/src/main/scala/sbt/internal/inc/consistent/ConsistentFileAnalysisStore.scala]]
 *
 * The only difference is that it doesn't write the analysis store to the temporary directory before copying it to the
 * destination. Doing so constitutes a write outside the sandbox directory provided during the work request, which
 * violates the Bazel multiplex worker protocol.
 */
object ConsistentFileAnalysisStore {
  def text(
    file: File,
    mappers: ReadWriteMappers,
    reproducible: Boolean = true,
    parallelism: Int = Runtime.getRuntime.availableProcessors(),
  ): XAnalysisStore =
    new AStore(
      file,
      new ConsistentAnalysisFormat(mappers, reproducible),
      SerializerFactory.text,
      parallelism,
    )

  def binary(file: File): XAnalysisStore =
    binary(
      file,
      mappers = ReadWriteMappers.getEmptyMappers(),
      reproducible = true,
    )

  def binary(
    file: File,
    mappers: ReadWriteMappers,
  ): XAnalysisStore =
    binary(
      file,
      mappers,
      reproducible = true,
    )

  def binary(
    file: File,
    mappers: ReadWriteMappers,
    reproducible: Boolean,
    parallelism: Int = Runtime.getRuntime.availableProcessors(),
  ): XAnalysisStore =
    new AStore(
      file,
      new ConsistentAnalysisFormat(mappers, reproducible),
      SerializerFactory.binary,
      parallelism,
    )

  private final class AStore[S <: Serializer, D <: Deserializer](
    file: File,
    format: ConsistentAnalysisFormat,
    sf: SerializerFactory[S, D],
    parallelism: Int = Runtime.getRuntime.availableProcessors(),
  ) extends XAnalysisStore {

    def set(analysisContents: AnalysisContents): Unit = {
      val analysis = analysisContents.getAnalysis
      val setup = analysisContents.getMiniSetup
      if (!file.getParentFile.exists()) Files.createDirectories(file.getParentFile.toPath)
      val fout = new FileOutputStream(file)
      try {
        val gout = new ParallelGzipOutputStream(fout, parallelism)
        val ser = sf.serializerFor(gout)
        format.write(ser, analysis, setup)
        gout.close()
      } finally fout.close
    }

    def get(): Optional[AnalysisContents] = {
      import sbt.internal.inc.JavaInterfaceUtil.EnrichOption
      allCatch.opt(unsafeGet()).toJava
    }

    def unsafeGet(): AnalysisContents =
      Using.gzipInputStream(new FileInputStream(file)) { in =>
        val deser = sf.deserializerFor(in)
        val (analysis, setup) = format.read(deser)
        AnalysisContents.create(analysis, setup)
      }
  }
}
