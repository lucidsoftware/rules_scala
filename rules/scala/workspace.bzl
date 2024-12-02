def _toolchain_configuration_repository_impl(repository_ctx):
    repository_ctx.file(
        "BUILD",
        """\
load(":default.bzl", "default_scala_toolchain_name")
load("@bazel_skylib//:bzl_library.bzl", "bzl_library")
load("@bazel_skylib//rules:common_settings.bzl", "string_setting")

string_setting(
    name = "original-scala-toolchain",
    build_setting_default = "",
    visibility = ["//visibility:public"],
)

string_setting(
    name = "scala-toolchain",
    build_setting_default = default_scala_toolchain_name,
    visibility = ["//visibility:public"],
)

bzl_library(
    name = "default",
    srcs = ["default.bzl"],
    visibility = ["//visibility:public"],
)
""",
    )

    repository_ctx.file(
        "default.bzl",
        "default_scala_toolchain_name = \"{}\"\n".format(
            repository_ctx.attr.default_scala_toolchain_name,
        ),
    )

_toolchain_configuration_repository = repository_rule(
    attrs = {
        "default_scala_toolchain_name": attr.string(mandatory = True),
    },
    doc = "Defines a setting for the Scala toolchain to use. This is done in a separate repository so we can provide the default dynamically.",
    implementation = _toolchain_configuration_repository_impl,
)

def scala_register_toolchains(default_scala_toolchain_name, toolchains = []):
    """Registers the provided Scala toolchains with Bazel and sets a default one to use.

    Args:
        default_scala_toolchain_name: The name of the default Scala toolchain to use.
        toolchains: The toolchains to register.
    """

    _toolchain_configuration_repository(
        name = "rules_scala_annex_scala_toolchain",
        default_scala_toolchain_name = default_scala_toolchain_name,
    )

    native.register_toolchains(
        "//src/main/scala:annex_bootstrap_2_13",
        "//src/main/scala:annex_bootstrap_3",
        "//src/main/scala:annex_zinc_2_13",
        "//src/main/scala:annex_zinc_3",
        *toolchains
    )
