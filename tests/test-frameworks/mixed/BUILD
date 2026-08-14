load("@rules_scala_annex//rules:scala.bzl", "scala_test")

scala_test(
    name = "mixed_2_13",
    srcs = glob(["*.scala"]),
    scala_version = "2.13",
    shard_count = 2,
    tags = ["manual"],
    deps = [
        "@annex_test//:com_github_sbt_junit_interface",
        "@annex_test//:junit_junit",
        "@annex_test//:org_scalacheck_scalacheck_2_13",
        "@annex_test//:org_scalactic_scalactic_2_13",
        "@annex_test//:org_scalatest_scalatest_compatible",
        "@annex_test//:org_scalatest_scalatest_core_2_13",
        "@annex_test//:org_scalatest_scalatest_flatspec_2_13",
        "@annex_test//:org_specs2_specs2_common_2_13",
        "@annex_test//:org_specs2_specs2_core_2_13",
        "@annex_test//:org_specs2_specs2_matcher_2_13",
    ],
)

scala_test(
    name = "mixed_2_12",
    srcs = glob(["*.scala"]),
    scala_version = "2.12",
    shard_count = 2,
    tags = ["manual"],
    deps = [
        "@annex_test//:com_github_sbt_junit_interface",
        "@annex_test//:junit_junit",
        "@annex_test_2_12//:org_scalacheck_scalacheck_2_12",
        "@annex_test_2_12//:org_scalactic_scalactic_2_12",
        "@annex_test_2_12//:org_scalatest_scalatest_compatible",
        "@annex_test_2_12//:org_scalatest_scalatest_core_2_12",
        "@annex_test_2_12//:org_scalatest_scalatest_flatspec_2_12",
        "@annex_test_2_12//:org_specs2_specs2_common_2_12",
        "@annex_test_2_12//:org_specs2_specs2_core_2_12",
        "@annex_test_2_12//:org_specs2_specs2_matcher_2_12",
    ],
)
