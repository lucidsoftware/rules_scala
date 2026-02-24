load("//rules:default_bootstrap_toolchain.bzl", "default_bootstrap_toolchain")
load("//rules:register_toolchain.bzl", "register_zinc_toolchain")
load("//rules:scala.bzl", "scala_library")
load(
    "//rules/scala:classpaths.bzl",
    "compiler_classpath_2_13",
    "compiler_classpath_3",
    "runtime_classpath_2_13",
    "runtime_classpath_3",
)
load("//rules/scala:scalac_options.bzl", "scala2_global_scalacopts", "shared_global_scalacopts")
load("//rules/scala:versions.bzl", "scala_2_13_version", "scala_3_version")

register_zinc_toolchain(
    name = "annex_zinc_2_13",
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
    srcs = [],
    deps_used_whitelist = compiler_classpath_2_13,
    scala_version = "3",
    deps = compiler_classpath_2_13,
)

scala_library(
    name = "scala-sdk-3",
    srcs = [],
    deps_used_whitelist = compiler_classpath_3,
    scala_version = "3",
    deps = compiler_classpath_3,
)

default_bootstrap_toolchain(
    name = "annex_bootstrap_3",
    global_scalacopts = shared_global_scalacopts,
)

register_zinc_toolchain(
    name = "annex_zinc_3",
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
    visibility = ["//visibility:public"],
)

config_setting(
    name = "deps_direct_off",
    define_values = {
        "scala_deps_direct": "off",
    },
    visibility = ["//visibility:public"],
)
