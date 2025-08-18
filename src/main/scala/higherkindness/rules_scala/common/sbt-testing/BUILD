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
    scala_toolchain_name = "annex_zinc_3",
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
    scala_toolchain_name = "annex_bootstrap_3",
    visibility = ["//visibility:public"],
    deps = [
        "@annex//:org_playframework_play_json_3",
        "@annex//:org_scala_lang_modules_scala_xml_2_13",
        "@annex//:org_scala_sbt_compiler_interface",
        "@annex//:org_scala_sbt_test_interface",
        "@annex//:org_scala_sbt_zinc_core_2_13",
    ],
)

scala_format_test(
    name = "format",
    srcs = glob(["**/*.scala"]),
)
