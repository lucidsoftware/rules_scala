package higherkindness.rules_scala.common.sbt_testing

import play.api.libs.json.{Format, JsObject, Json, Reads, Writes}

case class TestsFileData(testsByFramework: Map[String, Seq[TestDefinition]])

object TestsFileData {

  /**
   * Unlike that produced by [[Json.format]], this [[Format]] is deterministic, which we need to fulfill the
   * reproducibility component of [[https://bazel.build/basics/hermeticity Bazel's contract]].
   */
  implicit val format: Format[TestsFileData] = Format[Map[String, Seq[TestDefinition]]](
    Reads.mapReads,
    Writes { testsByFramework =>
      JsObject(
        testsByFramework.view
          .map { case (framework, tests) => framework -> Json.toJson(tests.sortBy(_.name)) }
          .toList
          .sortBy { case (framework, _) => framework },
      )
    },
  ).bimap(TestsFileData(_), _.testsByFramework)
}
