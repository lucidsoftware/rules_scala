load(
    "@rules_scala_annex//rules:scala.bzl",
    "configure_bootstrap_scala",
    "configure_zinc_scala",
    "scala_library",
)
load(
    "@rules_scala_annex//rules/scala:workspace.bzl",
    "scala_2_13_version",
    "scala_3_version",
)

alias(
    name = "bootstrap",
    actual = ":bootstrap_2_13",
    visibility = ["//visibility:public"],
)

alias(
    name = "zinc",
    actual = ":zinc_2_13",
    visibility = ["//visibility:public"],
)

# Scala 2.13
compiler_classpath_2_13 = [
    "@annex//:org_scala_lang_scala_compiler",
    "@annex//:org_scala_lang_scala_library",
    "@annex//:org_scala_lang_scala_reflect",
]

runtime_classpath_2_13 = [
    "@annex//:org_scala_lang_scala_library",
]

shared_global_scalacopts = [
    "-deprecation",
    "-Wconf:any:error",
]

shared_scala2_global_scalacopts = [
    "-Xlint:_,-unused",
    "-Ytasty-reader",
    "-Xsource:3",
]

scala2_global_scalacopts = shared_global_scalacopts + shared_scala2_global_scalacopts

configure_bootstrap_scala(
    name = "bootstrap_2_13",
    compiler_classpath = compiler_classpath_2_13,
    global_scalacopts = scala2_global_scalacopts,
    runtime_classpath = runtime_classpath_2_13,
    version = scala_2_13_version,
    visibility = ["//visibility:public"],
)

configure_zinc_scala(
    name = "zinc_2_13",
    compiler_bridge = "@annex//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = compiler_classpath_2_13,
    global_scalacopts = scala2_global_scalacopts,
    runtime_classpath = runtime_classpath_2_13,
    version = scala_2_13_version,
    visibility = ["//visibility:public"],
)

# Scala 3

# Adding this, so we make sure to have a Scala library in the
# IntelliJ libraries, so we can get a Scala SDK on sync.
scala_library(
    name = "scala-sdk",
    deps_used_whitelist = [
        "@annex//:org_scala_lang_scala3_library",
    ],
    scala = ":zinc_3",
    deps = [
        "@annex//:org_scala_lang_scala3_library",
    ],
)

compiler_classpath_3 = [
    "@annex//:org_scala_lang_scala3_compiler_3",
    "@annex//:org_scala_lang_scala3_library_3",
]

runtime_classpath_3 = [
    "@annex//:org_scala_lang_scala3_library_3",
    "@annex//:org_scala_lang_scala3_interfaces",
    "@annex//:org_scala_lang_tasty_core_3",
]

configure_bootstrap_scala(
    name = "bootstrap_3",
    compiler_classpath = compiler_classpath_3,
    global_scalacopts = shared_global_scalacopts,
    runtime_classpath = runtime_classpath_3,
    use_ijar = False,
    version = scala_3_version,
    visibility = ["//visibility:public"],
)

configure_zinc_scala(
    name = "zinc_3",
    compiler_bridge = "@annex//:org_scala_lang_scala3_sbt_bridge",
    compiler_classpath = compiler_classpath_3,
    global_scalacopts = shared_global_scalacopts,
    runtime_classpath = runtime_classpath_3,
    use_ijar = False,
    version = scala_3_version,
    visibility = ["//visibility:public"],
)

config_setting(
    name = "deps_used_off",
    define_values = {
        "scala_deps_used": "off",
    },
)

config_setting(
    name = "deps_direct_off",
    define_values = {
        "scala_deps_direct": "off",
    },
)
