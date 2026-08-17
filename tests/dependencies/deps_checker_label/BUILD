load("@rules_scala_annex//rules:scala.bzl", "scala_import", "scala_library")

scala_library(
    name = "library",
    srcs = [],
    deps_checker_label = "//dependencies/deps_checker_label:library-alias",
    scala_version = "2.13",
)

alias(
    name = "library-alias",
    actual = ":library",
)

scala_library(
    name = "depends-on-library-alias",
    srcs = [],
    scala_version = "2.13",
    tags = ["manual"],
    deps = [":library-alias"],
)

scala_import(
    name = "import",
    deps_checker_label = "//dependencies/deps_checker_label:import-alias",
    jars = [],
)

alias(
    name = "import-alias",
    actual = ":import",
)

scala_library(
    name = "depends-on-import-alias",
    srcs = [],
    scala_version = "2.13",
    tags = ["manual"],
    deps = [":import-alias"],
)

scala_library(
    name = "depends-on-whitelisted-library-alias",
    srcs = [],
    deps_used_whitelist = [":library-alias"],
    scala_version = "2.13",
    deps = [":library-alias"],
)
