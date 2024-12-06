load("@bazel_skylib//:bzl_library.bzl", "bzl_library")

exports_files([
    "scala.bzl",
    "scala_proto.bzl",
    "scala_with_scalafmt.bzl",
    "scalafmt.bzl",
])

bzl_library(
    name = "bazel_tools_build_defs_repo",
    srcs = [
        "@bazel_tools//tools/build_defs/repo:cache.bzl",
        "@bazel_tools//tools/build_defs/repo:http.bzl",
        "@bazel_tools//tools/build_defs/repo:jvm.bzl",
        "@bazel_tools//tools/build_defs/repo:local.bzl",
        "@bazel_tools//tools/build_defs/repo:utils.bzl",
    ],
)

bzl_library(
    name = "scala-rules",
    srcs = glob(["**/*.bzl"]) + [
        "//rules/common:bzl",
        "//rules/jvm:bzl",
        "//rules/private:bzl",
        "//rules/scala:bzl",
        "//rules/scala_proto:bzl",
        "//rules/scalafmt:bzl",
    ],
    visibility = ["//visibility:public"],
    deps = [
        ":bazel_tools_build_defs_repo",
        "@bazel_skylib//lib:dicts",
        "@bazel_skylib//lib:paths",
        "@bazel_skylib//lib:shell",
        "@rules_java//toolchains:toolchain_utils",
        "@rules_jvm_external//:implementation",
    ],
)
