load("//rules:scala.bzl", "scala_library")
load("//rules:scalafmt.bzl", "scala_format_test")

scala_library(
    name = "test",
    srcs = glob(["**/*.scala"]),
    scala_toolchain_name = "annex_zinc_3",
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/scala/higherkindness/rules_scala/common/args",
        "//src/main/scala/higherkindness/rules_scala/common/classloaders",
        "//src/main/scala/higherkindness/rules_scala/common/sandbox",
        "//src/main/scala/higherkindness/rules_scala/common/sbt-testing:common",
        "@annex//:net_sourceforge_argparse4j_argparse4j",
        "@annex//:org_playframework_play_json_3",
        "@annex//:org_scala_sbt_test_interface",
    ],
)

scala_format_test(
    name = "format",
    srcs = glob(["**/*.scala"]),
)
