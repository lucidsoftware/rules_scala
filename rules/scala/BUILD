load("@bazel_skylib//rules:common_settings.bzl", "string_setting")

filegroup(
    name = "bzl",
    srcs = glob(["**/*.bzl"]),
    visibility = ["//rules:__pkg__"],
)

string_setting(
    name = "scala-toolchain",
    build_setting_default = "default-scala",
    visibility = ["//visibility:public"],
)

toolchain_type(
    name = "toolchain_type",
    visibility = ["//visibility:public"],
)
