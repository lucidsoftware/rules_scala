load("//rules:scala.bzl", "scala_library")
load("//rules:scalafmt.bzl", "scala_format_test")

scala_library(
    name = "interrupt",
    srcs = glob(["*.scala"]),
    scala_version = "bootstrap_3",
    visibility = ["//visibility:public"],
)

scala_format_test(
    name = "format",
    srcs = glob(["*.scala"]),
)
