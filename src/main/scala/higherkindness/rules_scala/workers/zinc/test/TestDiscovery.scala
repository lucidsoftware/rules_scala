package higherkindness.rules_scala.workers.zinc.test

import higherkindness.rules_scala.common.sbt_testing.TestAnnotatedFingerprint
import higherkindness.rules_scala.common.sbt_testing.TestDefinition
import higherkindness.rules_scala.common.sbt_testing.TestSubclassFingerprint
import sbt.testing.{AnnotatedFingerprint, Framework, SubclassFingerprint}
import scala.collection.mutable
import xsbt.api.Discovery
import xsbti.api.{AnalyzedClass, ClassLike, Definition}

class TestDiscovery(framework: Framework) {
  private val (annotatedPrints, subclassPrints) = {
    val annotatedPrints = mutable.ArrayBuffer.empty[TestAnnotatedFingerprint]
    val subclassPrints = mutable.ArrayBuffer.empty[TestSubclassFingerprint]
    framework.fingerprints.foreach {
      case fingerprint: AnnotatedFingerprint => annotatedPrints += TestAnnotatedFingerprint(fingerprint)
      case fingerprint: SubclassFingerprint  => subclassPrints += TestSubclassFingerprint(fingerprint)
      case _                                 => throw new Exception("Unexpected fingerprint during test discovery")
    }
    (annotatedPrints.toSet, subclassPrints.toSet)
  }

  private def definitions(classes: Set[AnalyzedClass]) = {
    classes.toSeq
      .flatMap(`class` => Seq(`class`.api.classApi, `class`.api.objectApi))
      .flatMap(api => Seq(api, api.structure.declared, api.structure.inherited))
      .collect { case cl: ClassLike if cl.topLevel => cl }
  }

  private def discover(definitions: Seq[Definition]) =
    Discovery(subclassPrints.map(_.superclassName), annotatedPrints.map(_.annotationName))(
      definitions,
    )

  def apply(classes: Set[AnalyzedClass]) =
    for {
      (definition, discovered) <- discover(definitions(classes))
      fingerprint <- subclassPrints.collect {
        case print if discovered.baseClasses(print.superclassName) && discovered.isModule == print.isModule => print
      } ++
        annotatedPrints.collect {
          case print if discovered.annotations(print.annotationName) && discovered.isModule == print.isModule => print
        }
    } yield new TestDefinition(definition.name, fingerprint)
}
