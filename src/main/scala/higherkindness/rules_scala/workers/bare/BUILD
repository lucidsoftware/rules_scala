load("//rules:scalafmt.bzl", "scala_format_test")

filegroup(
    name = "bare-srcs",
    srcs = glob(["*.scala"]),
    visibility = ["//visibility:public"],
)

scala_format_test(
    name = "format",
    srcs = glob(["*.scala"]),
)
