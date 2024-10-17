load("@rules_scala_annex//rules:register_toolchain.bzl", "register_zinc_toolchain")
load("@rules_scala_annex//rules:scala.bzl", "scala_library")
load("@rules_scala_annex//rules/scala:workspace.bzl", "scala_2_13_version", "scala_3_version")

register_zinc_toolchain(
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

register_zinc_toolchain(
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
    scala_toolchain_name = "scala_2_13_with_semanticdb",
    tags = ["manual"],
)

scala_library(
    name = "semanticdb-3",
    srcs = glob(["*.scala"]),
    scala_toolchain_name = "scala_3_with_semanticdb",
    tags = ["manual"],
)
