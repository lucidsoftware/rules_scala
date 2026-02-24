load("@rules_scala_annex//rules:scala.bzl", "scala_test")

scala_test(
    name = "bad",
    srcs = ["Test.scala"],
    scala_version = "2.13",
    deps = [
        "@annex_test//:org_specs2_specs2_common_2_13",
        "@annex_test//:org_specs2_specs2_core_2_13",
        "@annex_test//:org_specs2_specs2_matcher_2_13",
    ],
)

scala_test(
    name = "context",
    srcs = ["ContextTest.scala"],
    scala_version = "2.13",
    deps = [
        "@annex_test//:org_specs2_specs2_common_2_13",
        "@annex_test//:org_specs2_specs2_core_2_13",
        "@annex_test//:org_specs2_specs2_matcher_2_13",
    ],
)

scala_test(
    name = "good",
    srcs = ["Test.scala"],
    deps_used_whitelist = [
        "@annex_test//:org_scala_sbt_zinc_persist_2_13",
        "@annex_test//:org_scala_sbt_zinc_persist_core_assembly",
    ],
    scala_version = "2.13",
    deps = [
        "@annex_test//:org_scala_sbt_zinc_persist_2_13",
        "@annex_test//:org_scala_sbt_zinc_persist_core_assembly",
        "@annex_test//:org_specs2_specs2_common_2_13",
        "@annex_test//:org_specs2_specs2_core_2_13",
        "@annex_test//:org_specs2_specs2_matcher_2_13",
    ],
)
