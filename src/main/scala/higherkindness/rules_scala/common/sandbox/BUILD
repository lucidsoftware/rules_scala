load("//rules:scala.bzl", "scala_library")
load("//rules:scalafmt.bzl", "scala_format_test")

scala_library(
    name = "sandbox",
    srcs = glob(["*.scala"]),
    scala = "//src/main/scala:bootstrap",
    visibility = ["//visibility:public"],
)

scala_format_test(
    name = "format",
    srcs = glob(["*.scala"]),
)
