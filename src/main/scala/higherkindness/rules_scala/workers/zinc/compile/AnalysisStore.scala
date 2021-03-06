package higherkindness.rules_scala
package workers.zinc.compile

import com.google.devtools.build.buildjar.jarhelper.JarHelper
import com.trueaccord.lenses.{Lens, Mutation}
import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}
import java.io.{File, InputStream, OutputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, NoSuchFileException, Path, Paths}
import java.nio.file.attribute.FileTime
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import java.util.Optional
import sbt.internal.inc.binary.converters.{ProtobufReaders, ProtobufWriters}
import sbt.internal.inc.schema.Type.{Projection, Structure}
import sbt.internal.inc.{APIs, Analysis, Relations, SourceInfos, Stamper, Stamps, schema, Stamp => StampImpl}
import sbt.internal.inc.schema.{AnalyzedClass, Annotation, ClassDependencies, ClassLike, Companions, NameHash, Type, TypeParameter, UsedName, UsedNames, Values}
import sbt.io.IO
import scala.math.Ordering
import scala.collection.mutable.StringBuilder
import scala.collection.immutable.TreeMap
import xsbti.compile.analysis.{GenericMapper, ReadMapper, ReadWriteMappers, Stamp, WriteMapper}
import xsbti.compile.{AnalysisContents, AnalysisStore, MiniSetup}

case class AnalysisFiles(apis: Path, miniSetup: Path, relations: Path, sourceInfos: Path, stamps: Path)

object AnxAnalysisStore {
  trait Format {
    def read[A <: GeneratedMessage with Message[A]](message: GeneratedMessageCompanion[A], inputStream: InputStream): A
    def write(message: GeneratedMessage, stream: OutputStream)
  }
  object BinaryFormat extends Format {
    def read[A <: GeneratedMessage with Message[A]](companion: GeneratedMessageCompanion[A], stream: InputStream) =
      companion.parseFrom(new GZIPInputStream(stream))
    def write(message: GeneratedMessage, stream: OutputStream) = {
      val gzip = new GZIPOutputStream(stream, true)
      message.writeTo(gzip)
      gzip.finish()
    }
  }
  object TextFormat extends Format {
    def read[A <: GeneratedMessage with Message[A]](companion: GeneratedMessageCompanion[A], stream: InputStream) =
      companion.fromAscii(IO.readStream(stream, StandardCharsets.US_ASCII))
    def write(message: GeneratedMessage, stream: OutputStream) = {
      val writer = new OutputStreamWriter(stream, StandardCharsets.US_ASCII)
      try writer.write(message.toString)
      finally writer.close()
    }
  }
}

trait Readable[A] {
  def read(file: Path): A
}

trait Writeable[A] {
  def write(file: Path, value: A)
}

class Store[A](readStream: InputStream => A, writeStream: (OutputStream, A) => Unit)
    extends Readable[A]
    with Writeable[A] {
  def read(file: Path) = {
    val stream = Files.newInputStream(file)
    try {
      readStream(stream)
    } finally {
      stream.close()
    }
  }
  def write(file: Path, value: A) = {
    val stream = Files.newOutputStream(file)
    try {
      writeStream(stream, value)
    } finally {
      stream.close()
    }
  }
}

class AnxAnalysisStore(files: AnalysisFiles, analyses: AnxAnalyses) extends AnalysisStore {
  def get() = {
    try {
      val analysis = Analysis.Empty.copy(
        apis = analyses.apis.read(files.apis),
        relations = analyses.relations.read(files.relations),
        infos = analyses.sourceInfos.read(files.sourceInfos),
        stamps = analyses.stamps.read(files.stamps)
      )
      val miniSetup = analyses.miniSetup.read(files.miniSetup)
      Optional.of(AnalysisContents.create(analysis, miniSetup))
    } catch {
      case e: NoSuchFileException => Optional.empty()
    }
  }

  def set(analysisContents: AnalysisContents) = {
    val analysis = analysisContents.getAnalysis.asInstanceOf[Analysis]
    analyses.apis.write(files.apis, analysis.apis)
    analyses.relations.write(files.relations, analysis.relations)
    analyses.sourceInfos.write(files.sourceInfos, analysis.infos)
    analyses.stamps.write(files.stamps, analysis.stamps)
    val miniSetup = analysisContents.getMiniSetup
    analyses.miniSetup.write(files.miniSetup, miniSetup)
  }

}

class AnxAnalyses(format: AnxAnalysisStore.Format) {
  private[this] val mappers = AnxMapper.mappers(Paths.get(""))
  private[this] val reader = new ProtobufReaders(mappers.getReadMapper, schema.Version.V1)
  private[this] val writer = new ProtobufWriters(mappers.getWriteMapper)

  def sortProjection(value: Projection): Projection = {
    value.copy(
      id = value.id,
      prefix = value.prefix,
    )
  }

  // This should be done sometime
  // But I can't figure out how to sort a Type differently based on which subclass it's a member of
  def sortType[A <: Type](value: A): Type = {
    value
  }

  def sortAnnotation(annotation: Annotation): Annotation = {
    annotation.copy(
      arguments = annotation.arguments.sortWith(_.hashCode() > _.hashCode())
    )
  }

  def sortTypeParameter(typeParameter: TypeParameter): TypeParameter = {
    typeParameter.copy(
      annotations = typeParameter.annotations.map(sortAnnotation).sortWith(_.hashCode() > _.hashCode()),
      typeParameters = sortTypeParameters(typeParameter.typeParameters),
    )
  }

  def sortStructure(structure: Structure): Structure = {
    structure.copy(
      parents = structure.parents.sortWith(_.hashCode() < _.hashCode()),
      declared = structure.declared.sortWith(_.hashCode() < _.hashCode()),
      inherited = structure.inherited.sortWith(_.hashCode() < _.hashCode()),
    )
  }

  def sortTypeParameters(typeParameters: Seq[TypeParameter]): Seq[TypeParameter] = {
    typeParameters.map(sortTypeParameter).sortWith(_.hashCode() > _.hashCode())
  }

  def sortClassLike(classLike: ClassLike): ClassLike = {
    classLike.copy(
      annotations = classLike.annotations.map(sortAnnotation).sortWith(_.hashCode() > _.hashCode()),
      structure = classLike.structure.map(sortStructure),
      savedAnnotations = classLike.savedAnnotations.sorted,
      childrenOfSealedClass = classLike.childrenOfSealedClass.map(sortType).sortWith(_.hashCode() > _.hashCode()),
      typeParameters = sortTypeParameters(classLike.typeParameters),
    )
  }

  def sortCompanions(companions: Companions): Companions = {
    Companions(
      classApi = companions.classApi.map(sortClassLike),
      objectApi = companions.objectApi.map(sortClassLike),
    )
  }

  def sortAnalyzedClassMap(analyzedClassMap: Map[String, AnalyzedClass]): TreeMap[String, AnalyzedClass] = {
    TreeMap[String, AnalyzedClass]() ++ analyzedClassMap.mapValues { analyzedClass =>
      analyzedClass.copy(
        api = analyzedClass.api.map(sortCompanions),
        nameHashes = analyzedClass.nameHashes.sortWith(_.hashCode() > _.hashCode()),
      )
    }
  }

  def sortApis(apis: schema.APIs): schema.APIs = {
    apis.copy(
      internal = sortAnalyzedClassMap(apis.internal),
      external = sortAnalyzedClassMap(apis.external),
    )
  }

  def apis = new Store[APIs](
    stream => reader.fromApis(shouldStoreApis = true)(format.read(schema.APIs, stream)),
    (stream, value) => format.write(
      sortApis(writer.toApis(value, shouldStoreApis = true).update(apiFileWrite)),
      stream
    )
  )

  def miniSetup = new Store[MiniSetup](
    stream => reader.fromMiniSetup(format.read(schema.MiniSetup, stream)),
    (stream, value) => format.write(writer.toMiniSetup(value), stream)
  )

  object UsedNameOrdering extends Ordering[UsedName] {
    def compare(x: UsedName, y: UsedName): Int = {
      (x.name + x.scopes.toString) compare (y.name + y.scopes.toString)
    }
  }

  def sortValuesMap(m: Map[String, Values]): TreeMap[String, Values] = {
    TreeMap[String, Values]() ++ m.mapValues { value =>
      value.copy(
        values = value.values.sorted
      )
    }
  }

  def sortUsedNamesMap(usedNamesMap: Map[String, UsedNames]): TreeMap[String, UsedNames] = {
    TreeMap[String, UsedNames]() ++ usedNamesMap.mapValues { currentUsedNames =>
      currentUsedNames.copy(
        usedNames = currentUsedNames.usedNames.sorted(UsedNameOrdering)
      )
    }
  }

  def sortClassDependencies(classDependencies: ClassDependencies): ClassDependencies = {
    classDependencies.copy(
      internal = sortValuesMap(classDependencies.internal),
      external = sortValuesMap(classDependencies.external)
    )
  }

  def sortRelations(relations: schema.Relations): schema.Relations = {
    relations.copy(
      srcProd = sortValuesMap(relations.srcProd),
      libraryDep = sortValuesMap(relations.libraryDep),
      libraryClassName = sortValuesMap(relations.libraryClassName),
      classes = sortValuesMap(relations.classes),
      productClassName = sortValuesMap(relations.productClassName),
      names = sortUsedNamesMap(relations.names),
      memberRef = relations.memberRef.map(sortClassDependencies),
      localInheritance = relations.localInheritance.map(sortClassDependencies),
      inheritance = relations.inheritance.map(sortClassDependencies)
    )
  }

  def relations = new Store[Relations](
    stream =>
      reader.fromRelations(format.read(schema.Relations, stream).update(relationsMapper(mappers.getReadMapper))),
    (stream, value) => format.write(
        sortRelations(writer.toRelations(value).update(relationsMapper(mappers.getWriteMapper))),
        stream
    )
  )

  def sourceInfos = new Store[SourceInfos](
    stream => reader.fromSourceInfos(format.read(schema.SourceInfos, stream)),
    (stream, value) => format.write(writer.toSourceInfos(value), stream)
  )

  def stamps = new Store[Stamps](
    stream => reader.fromStamps(format.read(schema.Stamps, stream)),
    (stream, value) => format.write(writer.toStamps(value), stream)
  )

  private[this] val apiFileWrite: Lens[schema.APIs, schema.APIs] => Mutation[schema.APIs] =
    _.update(
      _.internal.foreachValue(_.compilationTimestamp := JarHelper.DEFAULT_TIMESTAMP),
      _.external.foreachValue(_.compilationTimestamp := JarHelper.DEFAULT_TIMESTAMP)
    )

  // Workaround for https://github.com/sbt/zinc/pull/532
  private[this] def relationsMapper(
    mapper: GenericMapper
  ): Lens[schema.Relations, schema.Relations] => Mutation[schema.Relations] =
    _.update(
      _.srcProd.foreach(_.modify {
        case (source, products) =>
          mapper.mapSourceFile(new File(source)).toString ->
            products.update(_.values.foreach(_.modify(path => mapper.mapProductFile(new File(path)).toString)))
      }),
      _.libraryDep.foreach(_.modify {
        case (source, binaries) =>
          mapper.mapSourceFile(new File(source)).toString ->
            binaries.update(_.values.foreach(_.modify(path => mapper.mapBinaryFile(new File(path)).toString)))
      }),
      _.libraryDep.foreach(_.modify {
        case (binary, values) => mapper.mapBinaryFile(new File(binary)).toString -> values
      }),
      _.classes.foreach(_.modify {
        case (source, values) => mapper.mapSourceFile(new File(source)).toString -> values
      })
    )
}

object AnxMapper {
  val rootPlaceholder = Paths.get("_ROOT_")
  def mappers(root: Path) = new ReadWriteMappers(new AnxReadMapper(root), new AnxWriteMapper(root))
  private[this] val stampCache = new scala.collection.mutable.HashMap[Path, (FileTime, Stamp)]
  def hashStamp(file: Path) = {
    val newTime = Files.getLastModifiedTime(file)
    stampCache.get(file) match {
      case Some((time, stamp)) if newTime.compareTo(time) <= 0 => stamp
      case _ =>
        val stamp = Stamper.forHash(file.toFile)
        stampCache += (file -> (newTime, stamp))
        stamp
    }
  }
}

final class AnxWriteMapper(root: Path) extends WriteMapper {
  private[this] val rootAbs = root.toAbsolutePath

  private[this] def mapFile(file: File) = {
    val path = file.toPath
    if (path.startsWith(rootAbs)) AnxMapper.rootPlaceholder.resolve(rootAbs.relativize(path)).toFile else file
  }

  def mapBinaryFile(binaryFile: File) = mapFile(binaryFile)
  def mapBinaryStamp(file: File, binaryStamp: Stamp) = AnxMapper.hashStamp(file.toPath)
  def mapClasspathEntry(classpathEntry: File) = mapFile(classpathEntry)
  def mapJavacOption(javacOption: String) = javacOption
  def mapMiniSetup(miniSetup: MiniSetup) = miniSetup
  def mapProductFile(productFile: File) = mapFile(productFile)
  def mapProductStamp(file: File, productStamp: Stamp) =
    StampImpl.fromString(s"lastModified(${JarHelper.DEFAULT_TIMESTAMP})")
  def mapScalacOption(scalacOption: String) = scalacOption
  def mapSourceDir(sourceDir: File) = mapFile(sourceDir)
  def mapSourceFile(sourceFile: File) = mapFile(sourceFile)
  def mapSourceStamp(file: File, sourceStamp: Stamp) = sourceStamp
  def mapOutputDir(outputDir: File) = mapFile(outputDir)
}

final class AnxReadMapper(root: Path) extends ReadMapper {
  private[this] val rootAbs = root.toAbsolutePath

  private[this] def mapFile(file: File) = {
    val path = file.toPath
    if (path.startsWith(AnxMapper.rootPlaceholder)) rootAbs.resolve(AnxMapper.rootPlaceholder.relativize(path)).toFile
    else file
  }

  def mapBinaryFile(file: File) = mapFile(file)
  def mapBinaryStamp(file: File, stamp: Stamp) =
    if (AnxMapper.hashStamp(file.toPath) == stamp) Stamper.forLastModified(file) else stamp
  def mapClasspathEntry(classpathEntry: File) = mapFile(classpathEntry)
  def mapJavacOption(javacOption: String) = javacOption
  def mapOutputDir(dir: File) = mapFile(dir)
  def mapMiniSetup(miniSetup: MiniSetup) = miniSetup
  def mapProductFile(file: File) = mapFile(file)
  def mapProductStamp(file: File, productStamp: Stamp) = Stamper.forLastModified(file)
  def mapScalacOption(scalacOption: String) = scalacOption
  def mapSourceDir(sourceDir: File) = mapFile(sourceDir)
  def mapSourceFile(sourceFile: File) = mapFile(sourceFile)
  def mapSourceStamp(file: File, sourceStamp: Stamp) = sourceStamp
}
