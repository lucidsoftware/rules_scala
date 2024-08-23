load("//rules:register_toolchain.bzl", "register_bootstrap_toolchain", "register_zinc_toolchain")
load("//rules:scala.bzl", "scala_library")
load("//rules/scala:workspace.bzl", "scala_2_13_version", "scala_3_version")

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

register_bootstrap_toolchain(
    name = "bootstrap_2_13",
    compiler_classpath = compiler_classpath_2_13,
    global_scalacopts = scala2_global_scalacopts,
    runtime_classpath = runtime_classpath_2_13,
    version = scala_2_13_version,
    visibility = ["//visibility:public"],
)

register_zinc_toolchain(
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
    deps_used_whitelist = compiler_classpath_2_13,
    scala_toolchain_name = ":zinc_3",
    deps = compiler_classpath_2_13,
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

scala_library(
    name = "scala-sdk-3",
    deps_used_whitelist = compiler_classpath_3,
    scala_toolchain_name = "zinc_3",
    deps = compiler_classpath_3,
)

register_bootstrap_toolchain(
    name = "bootstrap_3",
    compiler_classpath = compiler_classpath_3,
    global_scalacopts = shared_global_scalacopts,
    runtime_classpath = runtime_classpath_3,
    use_ijar = True,
    version = scala_3_version,
    visibility = ["//visibility:public"],
)

register_zinc_toolchain(
    name = "zinc_3",
    compiler_bridge = "@annex//:org_scala_lang_scala3_sbt_bridge",
    compiler_classpath = compiler_classpath_3,
    global_scalacopts = shared_global_scalacopts,
    runtime_classpath = runtime_classpath_3,
    use_ijar = True,
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
