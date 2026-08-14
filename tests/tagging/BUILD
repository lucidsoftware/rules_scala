load("@rules_scala_annex//rules:scala.bzl", "scala_binary", "scala_library", "scala_test")

scala_binary(
    name = "binary-all-propagatable",
    srcs = ["A.scala"],
    scala_version = "2.13",
    tags = [
        "no-cache",
        "no-remote",
        "no-remote-cache",
        "no-remote-exec",
        "no-sandbox",
    ],
)

scala_library(
    name = "library-no-propagatable",
    srcs = ["A.scala"],
    scala_version = "2.13",
    tags = [
        "requires-network",
    ],
)

scala_test(
    name = "test-no-remote-only",
    srcs = ["A.scala"],
    scala_version = "2.13",
    tags = [
        "no-remote",
    ],
)
