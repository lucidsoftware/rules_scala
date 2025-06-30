package higherkindness.rules_scala.workers.common

import com.google.devtools.build.buildjar.jarhelper.JarHelper
import java.nio.file.{Path, Paths}
import sbt.internal.inc.{FarmHash, Hash, LastModified, PlainVirtualFile, PlainVirtualFileConverter}
import xsbti.VirtualFileRef
import xsbti.compile.MiniSetup
import xsbti.compile.analysis.{ReadMapper, ReadWriteMappers, Stamp, WriteMapper}

object AnnexMapper {
  val rootPlaceholder = Paths.get("_ROOT_")
  def mappers(root: Path) = {
    new ReadWriteMappers(new AnxReadMapper(root), new AnxWriteMapper(root))
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
      case _ => throw new Exception(s"Unexpected Stamp type encountered when writing. ${stamp.getClass} -- $stamp")
    }
  }
}

final class AnxWriteMapper(root: Path) extends WriteMapper {
  private val rootAbs = root.toAbsolutePath().normalize()

  private def mapFile(path: Path): Path = {
    if (path.toAbsolutePath().normalize().startsWith(rootAbs)) {
      AnnexMapper.rootPlaceholder.resolve(rootAbs.relativize(path.toAbsolutePath().normalize()))
    } else {
      path
    }
  }

  private def mapFile(virtualFileRef: VirtualFileRef): Path = {
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

final class AnxReadMapper(root: Path) extends ReadMapper {
  private val rootAbs = root.toAbsolutePath().normalize()

  private def mapFile(virtualFileRef: VirtualFileRef): Path = {
    mapFile(PlainVirtualFileConverter.converter.toPath(virtualFileRef))
  }

  private def mapFile(path: Path): Path = {
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

  override def mapSourceStamp(file: VirtualFileRef, sourceStamp: Stamp): Stamp = sourceStamp
  override def mapBinaryStamp(file: VirtualFileRef, binaryStamp: Stamp): Stamp = binaryStamp
  override def mapProductStamp(file: VirtualFileRef, productStamp: Stamp): Stamp = productStamp

  override def mapMiniSetup(miniSetup: MiniSetup): MiniSetup = miniSetup
}
