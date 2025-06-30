package higherkindness.rules_scala.workers.zinc.compile

import java.io.File
import java.nio.file.Path
import java.util
import java.util.Optional
import sbt.internal.inc.SourceInfos
import scala.collection.mutable.ArrayBuffer
import xsbti.*
import xsbti.api.{ClassLike, DependencyContext}
import xsbti.compile.analysis.ReadSourceInfos

case class AnnexAnalysis(
  apis: ArrayBuffer[ClassLike] = ArrayBuffer.empty,
  mainClasses: ArrayBuffer[String] = ArrayBuffer.empty,
  usedJars: ArrayBuffer[Path] = ArrayBuffer.empty,
) {
  def getCallback(converter: FileConverter): AnalysisCallback3 = new AnalysisCallback3 {
    override def api(sourceFile: File, classApi: ClassLike): Unit = apis += classApi
    override def api(sourceFile: VirtualFileRef, classApi: ClassLike): Unit = apis += classApi
    override def apiPhaseCompleted(): Unit = {}
    override def binaryDependency(
      onBinaryEntry: File,
      onBinaryClassName: String,
      fromClassName: String,
      fromSourceFile: File,
      context: DependencyContext,
    ): Unit = usedJars += onBinaryEntry.toPath

    override def binaryDependency(
      onBinaryEntry: Path,
      onBinaryClassName: String,
      fromClassName: String,
      fromSourceFile: VirtualFileRef,
      context: DependencyContext,
    ): Unit = usedJars += onBinaryEntry

    override def classDependency(onClassName: String, sourceClassName: String, context: DependencyContext): Unit = {}
    override def classesInOutputJar(): java.util.Set[String] = java.util.Collections.emptySet()
    override def dependencyPhaseCompleted(): Unit = {}
    override def enabled(): Boolean = true
    override def generatedLocalClass(source: File, classFile: File): Unit = {}
    override def generatedLocalClass(source: VirtualFileRef, classFile: Path): Unit = {}
    override def generatedNonLocalClass(
      source: File,
      classFile: File,
      binaryClassName: String,
      sourceClassName: String,
    ): Unit = {}

    override def generatedNonLocalClass(
      source: VirtualFileRef,
      classFile: Path,
      binaryClassName: String,
      sourceClassName: String,
    ): Unit = {}

    override def getPickleJarPair: Optional[T2[Path, Path]] = Optional.empty()

    /**
     * I don't really understand what this is supposed to do. It doesn't seem to be used in the Scala 3 compiler; the
     * only place I could find it being used is the Scala 2 compiler bridge (the implementation of the compiler
     * interface for Scala 2):
     * [[https://github.com/sbt/zinc/blob/75d54b672adbbda1b528f5759235704de1ba333f/internal/compiler-bridge/src/main/scala/xsbt/CompilerBridge.scala#L196]]
     *
     * It seems that when there are compilation problems, the Scala 2 compiler bridge invokes this method, shoves the
     * result in a `xsbt.InterfaceCompileFailed2`, throws that exception. That doesn't seem very useful, since we
     * could've just called it ourselves.
     */
    override def getSourceInfos: ReadSourceInfos = SourceInfos.empty
    override def isPickleJava: Boolean = false
    override def mainClass(sourceFile: File, className: String): Unit = mainClasses += className
    override def mainClass(sourceFile: VirtualFileRef, className: String): Unit = mainClasses += className
    override def problem(what: String, pos: Position, message: String, severity: Severity, reported: Boolean): Unit = {}
    override def problem2(
      what: String,
      position: Position,
      message: String,
      severity: Severity,
      reported: Boolean,
      rendered: Optional[String],
      diagnosticCode: Optional[DiagnosticCode],
      diagnosticRelatedInformation: util.List[DiagnosticRelatedInformation],
      actions: util.List[Action],
    ): Unit = {}

    override def startSource(source: File): Unit = {}
    override def startSource(source: VirtualFile): Unit = {}
    override def toVirtualFile(path: Path): VirtualFile = converter.toVirtualFile(path)
    override def usedName(className: String, name: String, useScopes: java.util.EnumSet[UseScope]): Unit = {}
  }
}
