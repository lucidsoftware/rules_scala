load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_scala_annex//rules/scala:workspace.bzl", "scalapb_version", "zinc_version")

specs2_version = "4.20.9"
scalatest_version = "3.2.19"
scalacheck_version = "1.18.1"

def test_artifacts():
    return [
        "com.thesamet.scalapb:lenses_2.13:{}".format(scalapb_version),
        "com.thesamet.scalapb:scalapb-runtime_2.13:{}".format(scalapb_version),
        "org.scala-lang.modules:scala-xml_2.13:2.3.0",
        "org.scala-sbt:compiler-interface:{}".format(zinc_version),
        "org.scalacheck:scalacheck_2.13:{}".format(scalacheck_version),
        "org.scalameta:semanticdb-scalac_2.13.14:4.9.9",
        "org.scalactic:scalactic_2.13:{}".format(scalatest_version),
        "org.scalatest:scalatest_2.13:{}".format(scalatest_version),
        "org.specs2:specs2-common_2.13:{}".format(specs2_version),
        "org.specs2:specs2-core_2.13:{}".format(specs2_version),
        "org.specs2:specs2-matcher_2.13:{}".format(specs2_version),
        "org.typelevel:kind-projector_2.13.14:0.13.3",
    ]

def test_dependencies():
    maven_install(
        name = "annex_test",
        artifacts = test_artifacts(),
        repositories = [
            "https://repo.maven.apache.org/maven2",
            "https://maven-central.storage-download.googleapis.com/maven2",
            "https://mirror.bazel.build/repo1.maven.org/maven2",
        ],
        fetch_sources = True,
        maven_install_json = "@rules_scala_annex_test//:annex_test_install.json",
    )
