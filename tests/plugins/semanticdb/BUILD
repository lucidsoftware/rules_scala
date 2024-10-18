load(":rule.bzl", "read_semanticdb_info")
load("@rules_scala_annex//rules:scala.bzl", "configure_zinc_scala", "scala_library")
load("@rules_scala_annex//rules/scala:workspace.bzl", "scala_2_13_version", "scala_3_version")

configure_zinc_scala(
    name = "scala_2_13_with_semanticdb",
    compiler_bridge = "@annex//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = [
        "@annex//:org_scala_lang_scala_compiler",
        "@annex//:org_scala_lang_scala_reflect",
        "@annex//:org_scala_lang_scala_library",
    ],
    global_plugins = ["@annex_test//:org_scalameta_semanticdb_scalac_2_13_14"],
    runtime_classpath = ["@annex//:org_scala_lang_scala_library"],
    semanticdb_bundle = False,
    version = scala_2_13_version,
)

configure_zinc_scala(
    name = "scala_3_with_semanticdb",
    compiler_bridge = "@annex//:org_scala_lang_scala3_sbt_bridge",
    compiler_classpath = [
        "@annex//:org_scala_lang_scala3_compiler_3",
        "@annex//:org_scala_lang_scala3_library_3",
    ],
    runtime_classpath = [
        "@annex//:org_scala_lang_scala3_library_3",
        "@annex//:org_scala_lang_scala3_interfaces",
        "@annex//:org_scala_lang_tasty_core_3",
    ],
    semanticdb_bundle = False,
    use_ijar = True,
    version = scala_3_version,
)

scala_library(
    name = "semanticdb-2_13",
    srcs = glob(["*.scala"]),
    scala = ":scala_2_13_with_semanticdb",
    tags = ["manual"],
)

read_semanticdb_info(
    name = "semanticdb-2_13-semanticdb-info",
    scala_target = ":semanticdb-2_13",
)

scala_library(
    name = "semanticdb-3",
    srcs = glob(["*.scala"]),
    scala = ":scala_3_with_semanticdb",
    tags = ["manual"],
)

read_semanticdb_info(
    name = "semanticdb-3-semanticdb-info",
    scala_target = ":semanticdb-3",
)

scala_library(
    name = "semanticdb-empty",
    srcs = [],
    scala = ":scala_2_13_with_semanticdb",
    tags = ["manual"],
)
