load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(
    name = "default",
    srcs = ["Deprecated.scala"],
    scala_version = "2.13",
)

scala_library(
    name = "fatal",
    srcs = ["Deprecated.scala"],
    scala_version = "2.13",
    scalacopts = [
        "-deprecation",
        "-Xfatal-warnings",
    ],
    tags = ["manual"],
)

scala_library(
    name = "warn",
    srcs = ["Deprecated.scala"],
    scala_version = "2.13",
    scalacopts = [
        "-deprecation",
    ],
)

scala_library(
    name = "fatal_glob",
    srcs = ["Deprecated.scala"],
    scala_version = "fatal-deprecation_2.13",
    tags = ["manual"],
)
