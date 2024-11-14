load("@rules_scala_annex//rules:register_toolchain.bzl", "register_zinc_toolchain")
load("@rules_scala_annex//rules:scala.bzl", "scala_library")
load("@rules_scala_annex//rules/scala:workspace.bzl", "scala_2_13_version")

# used_deps off
register_zinc_toolchain(
    name = "test_zinc_2_13_used_deps_off",
    compiler_bridge = "@annex//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = [
        "@annex//:org_scala_lang_scala_compiler",
        "@annex//:org_scala_lang_scala_reflect",
        "@annex//:org_scala_lang_scala_library",
    ],
    deps_used = "off",
    global_scalacopts = ["-Ytasty-reader"],
    runtime_classpath = ["@annex//:org_scala_lang_scala_library"],
    version = scala_2_13_version,
    visibility = ["//visibility:public"],
)

scala_library(
    name = "used_deps_off_a",
    srcs = ["A.scala"],
    scala_toolchain_name = "test_zinc_2_13_used_deps_off",
)

scala_library(
    name = "used_deps_off_b",
    srcs = ["B.scala"],
    scala_toolchain_name = "test_zinc_2_13_used_deps_off",
    deps = [":used_deps_off_a"],
)

scala_library(
    name = "used_deps_off_c",
    srcs = ["C.scala"],
    scala_toolchain_name = "test_zinc_2_13_used_deps_off",
    deps = [":used_deps_off_b"],
)

# direct_deps off
register_zinc_toolchain(
    name = "test_zinc_2_13_direct_deps_off",
    compiler_bridge = "@annex//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = [
        "@annex//:org_scala_lang_scala_compiler",
        "@annex//:org_scala_lang_scala_reflect",
        "@annex//:org_scala_lang_scala_library",
    ],
    deps_direct = "off",
    global_scalacopts = ["-Ytasty-reader"],
    runtime_classpath = ["@annex//:org_scala_lang_scala_library"],
    version = scala_2_13_version,
    visibility = ["//visibility:public"],
)

scala_library(
    name = "direct_deps_off_a",
    srcs = ["A.scala"],
    scala_toolchain_name = "test_zinc_2_13_direct_deps_off",
)

scala_library(
    name = "direct_deps_off_b",
    srcs = ["B.scala"],
    scala_toolchain_name = "test_zinc_2_13_direct_deps_off",
    deps = [":direct_deps_off_a"],
)

scala_library(
    name = "direct_deps_off_c",
    srcs = ["C.scala"],
    scala_toolchain_name = "test_zinc_2_13_direct_deps_off",
    deps = [":direct_deps_off_b"],
)

# toggle used_deps and direct_deps via --define
scala_library(
    name = "define_a",
    srcs = ["A.scala"],
    scala_toolchain_name = "test_zinc_2_13",
)

scala_library(
    name = "define_b",
    srcs = ["B.scala"],
    scala_toolchain_name = "test_zinc_2_13",
    deps = [":define_a"],
)

scala_library(
    name = "define_c",
    srcs = ["C.scala"],
    scala_toolchain_name = "test_zinc_2_13",
    deps = [":define_b"],
)
