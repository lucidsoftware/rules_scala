load("//rules:scala.bzl", "scala_library")
load("//rules:scalafmt.bzl", "scala_format_test")

_subprocess_srcs = ["SubprocessRunner.scala"]

_common_srcs = [
    f
    for f in glob(["**/*.scala"])
    if f not in _subprocess_srcs
]

scala_library(
    name = "subprocess",
    srcs = _subprocess_srcs,
    scala = "//src/main/scala:zinc",
    visibility = ["//visibility:public"],
    deps = [
        ":common",
        "//src/main/scala/higherkindness/rules_scala/common/classloaders",
        "@annex//:org_scala_sbt_test_interface",
    ],
)

scala_library(
    name = "common",
    srcs = _common_srcs,
    deps_used_whitelist = [
        "@annex//:org_scala_lang_scala_library",
    ],
    scala = "//src/main/scala:zinc",
    visibility = ["//visibility:public"],
    deps = [
        "@annex//:org_scala_lang_modules_scala_xml_2_13",
        "@annex//:org_scala_sbt_test_interface",
    ],
)

scala_format_test(
    name = "format",
    srcs = glob(["**/*.scala"]),
)
