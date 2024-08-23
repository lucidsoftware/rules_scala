load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(
    name = "default",
    srcs = ["Deprecated.scala"],
    scala_toolchain_name = "test_zinc_2_13",
)

scala_library(
    name = "fatal",
    srcs = ["Deprecated.scala"],
    scala_toolchain_name = "test_zinc_2_13",
    scalacopts = [
        "-deprecation",
        "-Xfatal-warnings",
    ],
)

scala_library(
    name = "warn",
    srcs = ["Deprecated.scala"],
    scala_toolchain_name = "test_zinc_2_13",
    scalacopts = [
        "-deprecation",
    ],
)

scala_library(
    name = "fatal_glob",
    srcs = ["Deprecated.scala"],
    scala_toolchain_name = "test_zinc_2_13_fatal_deprecation_opt",
)
