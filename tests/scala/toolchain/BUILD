load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(
    name = "child",
    srcs = ["Child.scala"],
)

scala_library(
    name = "parent1",
    srcs = ["Parent.scala"],
    scala_version = "2.13",
    deps = [":child"],
)

scala_library(
    name = "parent2",
    srcs = ["Parent.scala"],
    scala_version = "3",
    deps = [":child"],
)
