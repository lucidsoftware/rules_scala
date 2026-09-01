load("//rules:intellij_aspect_compat.bzl", "intellij_scalac_classpath")

filegroup(
    name = "bzl",
    srcs = glob(["**/*.bzl"]),
    visibility = ["//rules:__pkg__"],
)

toolchain_type(
    name = "toolchain_type",
    visibility = ["//visibility:public"],
)

intellij_scalac_classpath(
    name = "intellij_scalac_classpath",
    visibility = ["//visibility:public"],
)
