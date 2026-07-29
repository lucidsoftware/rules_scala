load("@rules_java//java:defs.bzl", "java_library")
load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(
    name = "plain",
    srcs = [],
    resource_strip_prefix = "resources/reference_conf",
    resources = ["reference.conf"],
    scala_version = "2.13",
)

java_library(
    name = "plain_java",
    resource_strip_prefix = "resources/reference_conf",
    resources = ["reference.conf"],
)
