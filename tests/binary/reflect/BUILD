load("@rules_scala_annex//rules:scala.bzl", "scala_binary", "scala_library")

scala_library(
    name = "bad",
    srcs = ["Reflect.scala"],
    scala_version = "2.13",
    tags = ["manual"],
)

scala_binary(
    name = "good",
    srcs = ["Reflect.scala"],
    scala_version = "2.13",
    deps = [
        "@annex_test//:org_scala_lang_scala_reflect",
    ],
)
