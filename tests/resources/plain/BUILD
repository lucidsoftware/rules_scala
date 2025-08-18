load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(
    name = "plain",
    srcs = [],
    resources = glob(["**/*.txt"]),
    scala_toolchain_name = "test_zinc_2_13",
)

scala_library(
    name = "merged",
    srcs = [],
    resource_jars = [":plain"],
    resource_strip_prefix = "resources",
    resources = glob(["**/*.txt"]),
    scala_toolchain_name = "test_zinc_2_13",
)
