load("@rules_scala_annex//rules:scala.bzl", "scala_binary", "scala_library", "scala_test")

scala_binary(
    name = "binary-all-propagatable",
    srcs = ["A.scala"],
    scala_toolchain_name = "test_zinc_2_13",
    tags = ["manual"] + [
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
    scala_toolchain_name = "test_zinc_2_13",
    tags = [
        "manual",
        "requires-network",
    ],
)

scala_test(
    name = "test-no-remote-only",
    srcs = ["A.scala"],
    scala_toolchain_name = "test_zinc_2_13",
    tags = [
        "manual",
        "no-remote",
    ],
)
