load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(
    name = "required",
    srcs = ["Required.scala"],
    scala_version = "3",
)

scala_library(
    name = "unused",
    srcs = ["Unused.scala"],
    scala_version = "3",
)

scala_library(
    name = "middle",
    srcs = ["Middle.scala"],
    deps_used_whitelist = [":unused"],
    scala_version = "3",
    deps = [
        ":required",
        ":unused",
    ],
)

scala_library(
    name = "consumer",
    srcs = ["Consumer.scala"],
    scala_version = "3",
    deps = [":middle"],
)
