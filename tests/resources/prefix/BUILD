load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(
    name = "prefix",
    resource_strip_prefix = package_name(),
    resources = glob(["**/*.txt"]),
    scala_toolchain_name = "test_zinc_2_13",
)

scala_library(
    name = "outside",
    resource_strip_prefix = "{}/other".format(package_name()),
    resources = glob(["**/*.txt"]),
    scala_toolchain_name = "test_zinc_2_13",
)
