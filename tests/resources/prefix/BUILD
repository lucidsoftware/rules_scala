load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(
    name = "prefix",
    srcs = [],
    resource_strip_prefix = package_name(),
    resources = glob(["**/*.txt"]),
    scala_version = "2.13",
)

scala_library(
    name = "outside",
    srcs = [],
    resource_strip_prefix = "{}/other".format(package_name()),
    resources = glob(["**/*.txt"]),
    scala_version = "2.13",
    tags = ["manual"],
)
