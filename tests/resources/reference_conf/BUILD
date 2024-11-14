load("@rules_java//java:defs.bzl", "java_library")
load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(
    name = "plain",
    resource_strip_prefix = "resources/reference_conf",
    resources = ["reference.conf"],
    scala_toolchain_name = "test_zinc_2_13",
)

java_library(
    name = "plain_java",
    resource_strip_prefix = "resources/reference_conf",
    resources = ["reference.conf"],
)
