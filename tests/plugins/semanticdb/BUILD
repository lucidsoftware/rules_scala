load("@bazel_skylib//rules:copy_file.bzl", "copy_file")
load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_scala_annex//rules:register_toolchain.bzl", "register_zinc_toolchain")
load("@rules_scala_annex//rules:scala.bzl", "scala_library")
load("@rules_scala_annex//rules/scala:versions.bzl", "scala_2_13_version", "scala_3_version")
load(":rule.bzl", "read_semanticdb_info")

register_zinc_toolchain(
    name = "scala_2_13_with_semanticdb",
    compiler_bridge = "@annex_test//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = [
        "@annex_test//:org_scala_lang_scala_compiler",
        "@annex_test//:org_scala_lang_scala_reflect",
        "@annex_test//:org_scala_lang_scala_library",
    ],
    global_plugins = ["@annex_test//:org_scalameta_semanticdb_scalac_2_13_14"],
    global_scalacopts = ["-Wconf:any:error"],
    prefix = "semanticdb",
    runtime_classpath = ["@annex_test//:org_scala_lang_scala_library"],
    semanticdb_bundle = False,
    version = scala_2_13_version,
)

register_zinc_toolchain(
    name = "scala_3_with_semanticdb",
    compiler_bridge = "@annex_test//:org_scala_lang_scala3_sbt_bridge",
    compiler_classpath = [
        "@annex_test//:org_scala_lang_scala3_compiler_3",
        "@annex_test//:org_scala_lang_scala3_library_3",
    ],
    global_scalacopts = ["-Wconf:any:error"],
    prefix = "semanticdb",
    runtime_classpath = [
        "@annex_test//:org_scala_lang_scala3_library_3",
        "@annex_test//:org_scala_lang_scala3_interfaces",
        "@annex_test//:org_scala_lang_tasty_core_3",
    ],
    semanticdb_bundle = False,
    use_ijar = True,
    version = scala_3_version,
)

scala_library(
    name = "semanticdb-2_13",
    srcs = glob(["*.scala"]),
    scala_version = "semanticdb_2.13",
)

read_semanticdb_info(
    name = "semanticdb-2_13-semanticdb-info",
    scala_target = ":semanticdb-2_13",
)

scala_library(
    name = "semanticdb-3",
    srcs = glob(["*.scala"]),
    scala_version = "semanticdb_3",
)

read_semanticdb_info(
    name = "semanticdb-3-semanticdb-info",
    scala_target = ":semanticdb-3",
)

scala_library(
    name = "semanticdb-empty",
    srcs = [],
    scala_version = "semanticdb_2.13",
)

java_library(
    name = "sourcejar-library",
    resources = glob(["*.scala"]),
)

copy_file(
    name = "sourcejar",
    src = ":sourcejar-library",
    out = "sourcejar.srcjar",
)

scala_library(
    name = "semanticdb-sourcejar",
    srcs = [":sourcejar"],
    scala_version = "semanticdb_2.13",
)

read_semanticdb_info(
    name = "semanticdb-sourcejar-semanticdb-info",
    scala_target = ":semanticdb-sourcejar",
)
