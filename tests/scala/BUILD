load(
    "@rules_scala_annex//rules:register_toolchain.bzl",
    "register_bootstrap_toolchain",
    "register_zinc_toolchain",
)
load("@rules_scala_annex//rules:scala.bzl", "scala_library")
load("@rules_scala_annex//rules/scala:workspace.bzl", "scala_2_13_version", "scala_3_version")
load("@rules_scala_annex//rules/scala:workspace_2_12.bzl", "scala_2_12_version")

alias(
    name = "2_13",
    actual = ":zinc_2_13",
    visibility = ["//visibility:public"],
)

# zinc 2.12

compiler_classpath_2_12 = [
    "@annex_2_12//:org_scala_lang_scala_compiler",
    "@annex_2_12//:org_scala_lang_scala_reflect",
    "@annex_2_12//:org_scala_lang_scala_library",
]

runtime_classpath_2_12 = [
    "@annex_2_12//:org_scala_lang_scala_library",
]

register_bootstrap_toolchain(
    name = "test_bootstrap_2_12",
    compiler_classpath = compiler_classpath_2_12,
    runtime_classpath = runtime_classpath_2_12,
    version = scala_2_12_version,
    visibility = ["//visibility:public"],
)

register_zinc_toolchain(
    name = "test_zinc_2_12",
    compiler_bridge = "@annex_2_12//:org_scala_sbt_compiler_bridge_2_12",
    compiler_classpath = compiler_classpath_2_12,
    runtime_classpath = runtime_classpath_2_12,
    version = scala_2_12_version,
    visibility = ["//visibility:public"],
)

# zinc scala 2

compiler_classpath_2_13 = [
    "@annex//:org_scala_lang_scala_compiler",
    "@annex//:org_scala_lang_scala_reflect",
    "@annex//:org_scala_lang_scala_library",
]

runtime_classpath_2_13 = [
    "@annex//:org_scala_lang_scala_library",
]

register_bootstrap_toolchain(
    name = "test_bootstrap_2_13",
    compiler_classpath = compiler_classpath_2_13,
    runtime_classpath = runtime_classpath_2_13,
    version = scala_2_13_version,
    visibility = ["//visibility:public"],
)

register_zinc_toolchain(
    name = "test_zinc_2_13",
    compiler_bridge = "@annex//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = compiler_classpath_2_13,
    global_scalacopts = [
        "-Ytasty-reader",
    ],
    runtime_classpath = runtime_classpath_2_13,
    version = scala_2_13_version,
    visibility = ["//visibility:public"],
)

# zinc scala 3

compiler_classpath_3 = [
    "@annex//:org_scala_lang_scala3_compiler_3",
    "@annex//:org_scala_lang_scala3_library_3",
]

runtime_classpath_3 = [
    "@annex//:org_scala_lang_scala3_library_3",
    "@annex//:org_scala_lang_scala3_interfaces",
    "@annex//:org_scala_lang_tasty_core_3",
]

register_bootstrap_toolchain(
    name = "test_bootstrap_3",
    compiler_classpath = compiler_classpath_3,
    runtime_classpath = runtime_classpath_3,
    use_ijar = True,
    version = scala_3_version,
    visibility = ["//visibility:public"],
)

register_zinc_toolchain(
    name = "test_zinc_3",
    compiler_bridge = "@annex//:org_scala_lang_scala3_sbt_bridge",
    compiler_classpath = compiler_classpath_3,
    runtime_classpath = runtime_classpath_3,
    use_ijar = True,
    version = scala_3_version,
    visibility = ["//visibility:public"],
)

# For global scalacoptions test
register_zinc_toolchain(
    name = "test_zinc_2_13_fatal_deprecation_opt",
    compiler_bridge = "@annex//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = compiler_classpath_2_13,
    global_scalacopts = [
        "-deprecation",
        "-Xfatal-warnings",
    ],
    runtime_classpath = runtime_classpath_2_13,
    version = scala_2_13_version,
    visibility = ["//scalacopts/rule:__subpackages__"],
)

# for log_level test
register_zinc_toolchain(
    name = "test_zinc_2_13_log_level_debug",
    compiler_bridge = "@annex//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = compiler_classpath_2_13,
    log_level = "debug",
    runtime_classpath = runtime_classpath_2_13,
    version = scala_2_13_version,
    visibility = ["//visibility:public"],
)
