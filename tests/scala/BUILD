load("@rules_scala_annex//rules:default_bootstrap_toolchain.bzl", "default_bootstrap_toolchain")
load(
    "@rules_scala_annex//rules:register_toolchain.bzl",
    "register_zinc_toolchain",
)
load(
    "@rules_scala_annex//rules/scala:versions.bzl",
    "scala_2_12_version",
    "scala_2_13_version",
    "scala_3_version",
)
load("@rules_scala_annex//rules/scalafmt:register_toolchain.bzl", "register_scalafmt_toolchain")

# zinc 2.12

compiler_classpath_2_12 = [
    "@annex_test_2_12//:org_scala_lang_scala_compiler",
    "@annex_test_2_12//:org_scala_lang_scala_reflect",
    "@annex_test_2_12//:org_scala_lang_scala_library",
]

runtime_classpath_2_12 = [
    "@annex_test_2_12//:org_scala_lang_scala_library",
]

register_zinc_toolchain(
    name = "test_zinc_2_12",
    compiler_bridge = "@annex_test_2_12//:org_scala_sbt_compiler_bridge_2_12",
    compiler_classpath = compiler_classpath_2_12,
    runtime_classpath = runtime_classpath_2_12,
    version = scala_2_12_version,
    visibility = ["//visibility:public"],
)

# Scala 2

compiler_classpath_2_13 = [
    "@annex_test//:org_scala_lang_scala_compiler",
    "@annex_test//:org_scala_lang_scala_reflect",
    "@annex_test//:org_scala_lang_scala_library",
]

runtime_classpath_2_13 = [
    "@annex_test//:org_scala_lang_scala_library",
]

register_zinc_toolchain(
    name = "test_zinc_2_13",
    compiler_bridge = "@annex_test//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = compiler_classpath_2_13,
    global_scalacopts = [
        "-Ytasty-reader",
    ],
    runtime_classpath = runtime_classpath_2_13,
    version = scala_2_13_version,
    visibility = ["//visibility:public"],
)

# Scala 3

default_bootstrap_toolchain(
    name = "test_bootstrap_3",
)

compiler_classpath_3 = [
    "@annex_test//:org_scala_lang_scala3_compiler_3",
    "@annex_test//:org_scala_lang_scala3_library_3",
]

runtime_classpath_3 = [
    "@annex_test//:org_scala_lang_scala3_library_3",
    "@annex_test//:org_scala_lang_scala3_interfaces",
    "@annex_test//:org_scala_lang_tasty_core_3",
]

register_zinc_toolchain(
    name = "test_zinc_3",
    compiler_bridge = "@annex_test//:org_scala_lang_scala3_sbt_bridge",
    compiler_classpath = compiler_classpath_3,
    runtime_classpath = runtime_classpath_3,
    use_ijar = True,
    version = scala_3_version,
    visibility = ["//visibility:public"],
)

# For global scalacoptions test
register_zinc_toolchain(
    name = "test_zinc_2_13_fatal_deprecation_opt",
    compiler_bridge = "@annex_test//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = compiler_classpath_2_13,
    global_scalacopts = [
        "-deprecation",
        "-Xfatal-warnings",
    ],
    prefix = "fatal-deprecation",
    runtime_classpath = runtime_classpath_2_13,
    version = scala_2_13_version,
    visibility = ["//scalacopts/rule:__subpackages__"],
)

# for log_level test
register_zinc_toolchain(
    name = "test_zinc_2_13_log_level_debug",
    compiler_bridge = "@annex_test//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = compiler_classpath_2_13,
    log_level = "debug",
    prefix = "log-level-debug",
    runtime_classpath = runtime_classpath_2_13,
    version = scala_2_13_version,
    visibility = ["//visibility:public"],
)

# scalafmt toolchain covering all test scala versions including prefixed variants
register_scalafmt_toolchain(
    name = "test_scalafmt",
    config = "//:.scalafmt.conf",
    scala_versions = [
        scala_2_12_version,
        scala_2_13_version,
        scala_3_version,
        "bootstrap_" + scala_3_version,
        "direct-deps-off_" + scala_2_13_version,
        "fatal-deprecation_" + scala_2_13_version,
        "kind-projector_" + scala_2_13_version,
        "log-level-debug_" + scala_2_13_version,
        "semanticdb_" + scala_2_13_version,
        "semanticdb_" + scala_3_version,
        "used-deps-off_" + scala_2_13_version,
        "jvm-flags-bad-bootstrap_" + scala_3_version,
        "jvm-flags-good-bootstrap_" + scala_3_version,
        "jvm-flags-bad-zinc_" + scala_3_version,
        "jvm-flags-good-zinc_" + scala_3_version,
    ],
    visibility = ["//visibility:public"],
)
