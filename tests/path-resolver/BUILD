load("@rules_scala_annex//rules:scala.bzl", "scala_test")

scala_test(
    name = "specs",
    srcs = glob(["*.scala"]),
    scala_toolchain_name = "test_zinc_2_13",
    deps = [
        "@annex_test//:org_scalactic_scalactic_2_13",
        "@annex_test//:org_scalatest_scalatest_compatible",
        "@annex_test//:org_scalatest_scalatest_core_2_13",
        "@annex_test//:org_scalatest_scalatest_flatspec_2_13",
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/sandbox",
    ],
)
