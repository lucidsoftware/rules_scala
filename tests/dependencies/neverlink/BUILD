load("@rules_scala_annex//rules:scala.bzl", "scala_binary", "scala_library")

scala_library(
    name = "library",
    srcs = ["Library.scala"],
    neverlink = True,
    scala_toolchain_name = "test_zinc_2_13",
)

scala_binary(
    name = "binary",
    srcs = ["Binary.scala"],
    scala_toolchain_name = "test_zinc_2_13",
    deps = [":library"],
)
