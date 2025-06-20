load("//rules:scalafmt.bzl", "scala_format_test")

exports_files(["CompilerWrapper.scala"])

scala_format_test(
    name = "format",
    srcs = glob(["**/*.scala"]),
)
