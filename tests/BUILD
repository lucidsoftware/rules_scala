load(
    "@rules_java//toolchains:default_java_toolchain.bzl",
    "DEFAULT_TOOLCHAIN_CONFIGURATION",
    "default_java_toolchain",
)

default_java_toolchain(
    name = "repository_default_toolchain_21",
    configuration = DEFAULT_TOOLCHAIN_CONFIGURATION,
    java_runtime = "@rules_java//toolchains:remotejdk_21",
    javac_supports_worker_multiplex_sandboxing = True,
    source_version = "21",
    target_version = "21",
)
