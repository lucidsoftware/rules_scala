package higherkindness.rules_scala.common.sbt_testing

import play.api.libs.json.{Format, Json, Reads, Writes}
import sbt.testing.{AnnotatedFingerprint, Fingerprint, SubclassFingerprint}

sealed trait TestFingerprint extends Fingerprint

object TestFingerprint {
  implicit val format: Format[TestFingerprint] = Format(
    Reads(jsValue => jsValue.validate[TestAnnotatedFingerprint].orElse(jsValue.validate[TestSubclassFingerprint])),
    Writes {
      case annotated: TestAnnotatedFingerprint => Json.toJson(annotated)
      case subclass: TestSubclassFingerprint   => Json.toJson(subclass)
    },
  )
}

case class TestAnnotatedFingerprint(annotationName: String, isModule: Boolean)
    extends AnnotatedFingerprint
    with TestFingerprint

object TestAnnotatedFingerprint {
  implicit val format: Format[TestAnnotatedFingerprint] = Json.format[TestAnnotatedFingerprint]

  def apply(fingerprint: AnnotatedFingerprint) =
    new TestAnnotatedFingerprint(fingerprint.annotationName, fingerprint.isModule)
}

case class TestSubclassFingerprint(isModule: Boolean, requireNoArgConstructor: Boolean, superclassName: String)
    extends SubclassFingerprint
    with TestFingerprint

object TestSubclassFingerprint {
  implicit val format: Format[TestSubclassFingerprint] = Json.format[TestSubclassFingerprint]

  def apply(fingerprint: SubclassFingerprint) =
    new TestSubclassFingerprint(fingerprint.isModule, fingerprint.requireNoArgConstructor, fingerprint.superclassName)
}
