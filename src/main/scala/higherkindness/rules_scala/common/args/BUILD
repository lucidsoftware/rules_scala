load("//rules:scala.bzl", "scala_library")
load("//rules:scalafmt.bzl", "scala_format_test")

scala_library(
    name = "args",
    srcs = glob(["**/*.scala"]),
    scala_toolchain_name = "bootstrap_2_13",
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/scala/higherkindness/rules_scala/common/error",
        "@annex//:net_sourceforge_argparse4j_argparse4j",
    ],
)

scala_format_test(
    name = "format",
    srcs = glob(["**/*.scala"]),
)
