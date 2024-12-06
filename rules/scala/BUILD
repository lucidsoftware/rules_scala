load("@bazel_skylib//rules:common_settings.bzl", "string_flag", "string_setting")

filegroup(
    name = "bzl",
    srcs = glob(["**/*.bzl"]),
    visibility = ["//rules:__pkg__"],
)

toolchain_type(
    name = "toolchain_type",
    visibility = ["//visibility:public"],
)

string_setting(
    name = "original-scala-toolchain",
    build_setting_default = "",
    visibility = ["//visibility:public"],
)

string_flag(
    name = "scala-toolchain",
    build_setting_default = "annex_zinc_2_13",
    visibility = ["//visibility:public"],
)
