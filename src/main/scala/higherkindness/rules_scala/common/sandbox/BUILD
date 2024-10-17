load("//rules:scala.bzl", "scala_library")
load("//rules:scalafmt.bzl", "scala_format_test")

scala_library(
    name = "sandbox",
    srcs = glob(["*.scala"]),
    scala_toolchain_name = "bootstrap_2_13",
    visibility = ["//visibility:public"],
)

scala_format_test(
    name = "format",
    srcs = glob(["*.scala"]),
)
