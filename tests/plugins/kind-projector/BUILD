load("@rules_scala_annex//rules:scala.bzl", "configure_zinc_scala", "scala_library")
load("@rules_scala_annex//rules/scala:workspace.bzl", "scala_2_13_version")

scala_library(
    name = "kind-projector",
    srcs = ["KindProjector.scala"],
    plugins = [
        "@annex_test//:org_typelevel_kind_projector_2_13_14",
    ],
    tags = ["manual"],
)

configure_zinc_scala(
    name = "scala_2_13_with_kind_projector",
    compiler_bridge = "@annex//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = [
        "@annex//:org_scala_lang_scala_compiler",
        "@annex//:org_scala_lang_scala_reflect",
        "@annex//:org_scala_lang_scala_library",
    ],
    global_plugins = [
        "@annex_test//:org_typelevel_kind_projector_2_13_14",
    ],
    runtime_classpath = [
        "@annex//:org_scala_lang_scala_library",
    ],
    version = scala_2_13_version,
)

scala_library(
    name = "kind-projector-via-global-plugins",
    srcs = ["KindProjector.scala"],
    scala = ":scala_2_13_with_kind_projector",
    tags = ["manual"],
)
