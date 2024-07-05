package higherkindness.rules_scala
package workers.common

import com.google.devtools.build.buildjar.jarhelper.JarHelper
import java.io.{File, InputStream, OutputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, NoSuchFileException, Path, Paths}
import java.nio.file.attribute.FileTime
import java.util
import java.util.concurrent.ConcurrentHashMap
import java.util.LinkedHashMap
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import java.util.Optional
import sbt.internal.inc.binary.converters.{ProtobufReaders, ProtobufWriters}
import sbt.internal.inc.Schema.Type.{Projection, Structure}
import sbt.internal.inc.{APIs, Analysis, FarmHash, Hash, LastModified, PlainVirtualFile, PlainVirtualFileConverter, Relations, Schema, SourceInfos, Stamp => StampImpl, Stamper, Stamps}
import sbt.internal.inc.Schema.{Access, AnalyzedClass, Annotation, AnnotationArgument, ClassDefinition, ClassDependencies, ClassLike, Companions, MethodParameter, NameHash, ParameterList, Path => SchemaPath, Qualifier, Type, TypeParameter, UsedName, UsedNames, Values}
import sbt.internal.shaded.com.google.protobuf.GeneratedMessageV3
import sbt.io.IO
import scala.collection.immutable.TreeMap
import xsbti.compile.analysis.{GenericMapper, ReadMapper, ReadWriteMappers, Stamp, WriteMapper}
import xsbti.compile.{AnalysisContents, AnalysisStore, MiniSetup}
import scala.jdk.CollectionConverters._
import xsbti.VirtualFileRef
import java.util.Objects

// TODO: fix git for this file. Make it a mv to keep history.

object AnnexMapper {
  val rootPlaceholder = Paths.get("_ROOT_")
  def mappers(root: Path, isIncremental: Boolean) = {
    new ReadWriteMappers(new AnxReadMapper(root, isIncremental), new AnxWriteMapper(root))
  }

  /**
   * Gets a reproducible/consistent stamp that we can write to the analysis file and end up with reproducible output
   * across machines, jvms, builds, etc.
   *
   * Practically speaking, all we're doing is setting the timestamp in LastModified stamps to a constant value.
   */
  final def getConsistentWriteStamp(stamp: Stamp): Stamp = {
    stamp match {
      case farmHash: FarmHash         => farmHash
      case hash: Hash                 => hash
      case lastModified: LastModified => new LastModified(JarHelper.DEFAULT_TIMESTAMP)
      case _                          => throw new Exception("Unexpected Stamp type encountered when writing.")
    }
  }

  final def getReadStamp(file: VirtualFileRef, stamp: Stamp, isIncremental: Boolean): Stamp = {
    if (isIncremental) {
      getIncrementalModeReadStamp(file, stamp)
    } else {
      stamp
    }
  }

  /**
   * When in incremental mode we do not want to rely on the timestamp from the AnalysisStore because we're assuming it
   * was set to a constant value when written to the AnalysisStore.
   *
   * Instead, for any LastModified stamps, we read the file's time stamp from disk.
   */
  final def getIncrementalModeReadStamp(file: VirtualFileRef, stamp: Stamp): Stamp = {
    stamp match {
      case farmHash: FarmHash => farmHash
      case hash: Hash         => hash
      case lastModified: LastModified => {
        Stamper.forLastModifiedP(PlainVirtualFileConverter.converter.toPath(file))
      }
      case _ => throw new Exception("Unexpected Stamp type encountered when reading")
    }
  }
}

final class AnxWriteMapper(root: Path) extends WriteMapper {
  private[this] val rootAbs = root.toAbsolutePath

  private[this] def mapFile(path: Path): Path = {
    if (path.startsWith(rootAbs)) {
      AnnexMapper.rootPlaceholder.resolve(rootAbs.relativize(path))
    } else {
      path
    }
  }

  private[this] def mapFile(virtualFileRef: VirtualFileRef): Path = {
    mapFile(PlainVirtualFileConverter.converter.toPath(virtualFileRef))
  }

  override def mapSourceFile(sourceFile: VirtualFileRef): VirtualFileRef = PlainVirtualFile(mapFile(sourceFile))
  override def mapBinaryFile(binaryFile: VirtualFileRef): VirtualFileRef = PlainVirtualFile(mapFile(binaryFile))
  override def mapProductFile(productFile: VirtualFileRef): VirtualFileRef = PlainVirtualFile(mapFile(productFile))

  override def mapClasspathEntry(classpathEntry: Path): Path = mapFile(classpathEntry)
  override def mapJavacOption(javacOption: String): String = javacOption
  override def mapScalacOption(scalacOption: String): String = scalacOption

  override def mapOutputDir(outputDir: Path): Path = mapFile(outputDir)
  override def mapSourceDir(sourceDir: Path): Path = mapFile(sourceDir)

  override def mapSourceStamp(file: VirtualFileRef, sourceStamp: Stamp): Stamp = {
    AnnexMapper.getConsistentWriteStamp(sourceStamp)
  }
  override def mapBinaryStamp(file: VirtualFileRef, binaryStamp: Stamp): Stamp = {
    AnnexMapper.getConsistentWriteStamp(binaryStamp)
  }
  override def mapProductStamp(file: VirtualFileRef, productStamp: Stamp): Stamp = {
    AnnexMapper.getConsistentWriteStamp(productStamp)
  }

  override def mapMiniSetup(miniSetup: MiniSetup): MiniSetup = miniSetup
}

final class AnxReadMapper(root: Path, isIncremental: Boolean) extends ReadMapper {
  private[this] val rootAbs = root.toAbsolutePath

  private[this] def mapFile(virtualFileRef: VirtualFileRef): Path = {
    mapFile(PlainVirtualFileConverter.converter.toPath(virtualFileRef))
  }

  private[this] def mapFile(path: Path): Path = {
    if (path.startsWith(AnnexMapper.rootPlaceholder)) {
      rootAbs.resolve(AnnexMapper.rootPlaceholder.relativize(path))
    } else {
      path
    }
  }

  override def mapSourceFile(sourceFile: VirtualFileRef): VirtualFileRef = PlainVirtualFile(mapFile(sourceFile))
  override def mapBinaryFile(binaryFile: VirtualFileRef): VirtualFileRef = PlainVirtualFile(mapFile(binaryFile))
  override def mapProductFile(productFile: VirtualFileRef): VirtualFileRef = PlainVirtualFile(mapFile(productFile))

  override def mapClasspathEntry(classpathEntry: Path): Path = mapFile(classpathEntry)
  override def mapJavacOption(javacOption: String): String = javacOption
  override def mapScalacOption(scalacOption: String): String = scalacOption

  override def mapOutputDir(outputDir: Path): Path = mapFile(outputDir)
  override def mapSourceDir(sourceDir: Path): Path = mapFile(sourceDir)

  override def mapSourceStamp(file: VirtualFileRef, sourceStamp: Stamp): Stamp = {
    AnnexMapper.getReadStamp(file, sourceStamp, isIncremental)
  }
  override def mapBinaryStamp(file: VirtualFileRef, binaryStamp: Stamp): Stamp = {
    AnnexMapper.getReadStamp(file, binaryStamp, isIncremental)
  }
  override def mapProductStamp(file: VirtualFileRef, productStamp: Stamp): Stamp = {
    AnnexMapper.getReadStamp(file, productStamp, isIncremental)
  }

  override def mapMiniSetup(miniSetup: MiniSetup): MiniSetup = miniSetup
}
