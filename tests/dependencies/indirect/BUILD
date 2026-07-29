load("@rules_scala_annex//rules:register_toolchain.bzl", "register_zinc_toolchain")
load("@rules_scala_annex//rules:scala.bzl", "scala_library")
load("@rules_scala_annex//rules/scala:versions.bzl", "scala_2_13_version")

# used_deps off
register_zinc_toolchain(
    name = "test_zinc_2_13_used_deps_off",
    compiler_bridge = "@annex_test//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = [
        "@annex_test//:org_scala_lang_scala_compiler",
        "@annex_test//:org_scala_lang_scala_reflect",
        "@annex_test//:org_scala_lang_scala_library",
    ],
    deps_used = "off",
    global_scalacopts = ["-Ytasty-reader"],
    prefix = "used-deps-off",
    runtime_classpath = ["@annex_test//:org_scala_lang_scala_library"],
    version = scala_2_13_version,
    visibility = ["//visibility:public"],
)

scala_library(
    name = "used_deps_off_a",
    srcs = ["A.scala"],
    scala_version = "used-deps-off_2.13",
)

scala_library(
    name = "used_deps_off_b",
    srcs = ["B.scala"],
    scala_version = "used-deps-off_2.13",
    deps = [":used_deps_off_a"],
)

scala_library(
    name = "used_deps_off_c",
    srcs = ["C.scala"],
    scala_version = "used-deps-off_2.13",
    deps = [":used_deps_off_b"],
)

# direct_deps off
register_zinc_toolchain(
    name = "test_zinc_2_13_direct_deps_off",
    compiler_bridge = "@annex_test//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = [
        "@annex_test//:org_scala_lang_scala_compiler",
        "@annex_test//:org_scala_lang_scala_reflect",
        "@annex_test//:org_scala_lang_scala_library",
    ],
    deps_direct = "off",
    global_scalacopts = ["-Ytasty-reader"],
    prefix = "direct-deps-off",
    runtime_classpath = ["@annex_test//:org_scala_lang_scala_library"],
    version = scala_2_13_version,
    visibility = ["//visibility:public"],
)

scala_library(
    name = "direct_deps_off_a",
    srcs = ["A.scala"],
    scala_version = "direct-deps-off_2.13",
)

scala_library(
    name = "direct_deps_off_b",
    srcs = ["B.scala"],
    scala_version = "direct-deps-off_2.13",
    deps = [":direct_deps_off_a"],
)

scala_library(
    name = "direct_deps_off_c",
    srcs = ["C.scala"],
    scala_version = "direct-deps-off_2.13",
    deps = [":direct_deps_off_b"],
)

# toggle used_deps and direct_deps via --define
scala_library(
    name = "define_a",
    srcs = ["A.scala"],
    scala_version = "2.13",
)

scala_library(
    name = "define_b",
    srcs = ["B.scala"],
    scala_version = "2.13",
    deps = [":define_a"],
)

scala_library(
    name = "define_c",
    srcs = ["C.scala"],
    scala_version = "2.13",
    deps = [":define_b"],
)
