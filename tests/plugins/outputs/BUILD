load("@rules_scala_annex//rules:scala.bzl", "scala_library")
load(":rules.bzl", "my_plugin")

scala_library(
    name = "my_plugin_lib",
    srcs = ["plugin.scala"],
    scala_version = "2.13",
)

my_plugin(
    name = "my_plugin",
    deps = [
        ":my_plugin_lib",
    ],
)

scala_library(
    name = "usage",
    srcs = ["usage.scala"],
    plugins = [
        ":my_plugin",
    ],
    scala_version = "2.13",
)
