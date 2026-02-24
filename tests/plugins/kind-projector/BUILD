load("@rules_scala_annex//rules:register_toolchain.bzl", "register_zinc_toolchain")
load("@rules_scala_annex//rules:scala.bzl", "scala_library")
load("@rules_scala_annex//rules/scala:versions.bzl", "scala_2_13_version")

scala_library(
    name = "kind-projector",
    srcs = ["KindProjector.scala"],
    plugins = [
        "@annex_test//:org_typelevel_kind_projector_2_13_14",
    ],
    tags = ["manual"],
)

register_zinc_toolchain(
    name = "test_zinc_2_13_with_kind_projector",
    compiler_bridge = "@annex_test//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = [
        "@annex_test//:org_scala_lang_scala_compiler",
        "@annex_test//:org_scala_lang_scala_reflect",
        "@annex_test//:org_scala_lang_scala_library",
    ],
    global_plugins = [
        "@annex_test//:org_typelevel_kind_projector_2_13_14",
    ],
    prefix = "kind-projector",
    runtime_classpath = [
        "@annex_test//:org_scala_lang_scala_library",
    ],
    version = scala_2_13_version,
)

scala_library(
    name = "kind-projector-via-global-plugins",
    srcs = ["KindProjector.scala"],
    scala_version = "kind-projector_2.13",
    tags = ["manual"],
)
