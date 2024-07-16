load("@com_github_bazelbuild_buildtools//buildifier:def.bzl", "buildifier")
load(
    "@rules_java//toolchains:default_java_toolchain.bzl",
    "DEFAULT_TOOLCHAIN_CONFIGURATION",
    "default_java_toolchain",
)

default_java_toolchain(
    name = "repository_default_toolchain_21",
    configuration = DEFAULT_TOOLCHAIN_CONFIGURATION,
    java_runtime = "@rules_java//toolchains:remotejdk_21",
    source_version = "21",
    target_version = "21",
)

buildifier(
    name = "buildifier",
)

buildifier(
    name = "buildifier_check",
    mode = "check",
)
