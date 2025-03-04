load("//rules:register_toolchain.bzl", "scalafmt_toolchain_setting")

_ScalafmtConfig = provider(
    fields = {
        "config": "The Scalafmt configuration file.",
    },
)

def _scalafmt_config_impl(ctx):
    return [
        platform_common.ToolchainInfo(scalafmt_config = _ScalafmtConfig(config = ctx.file.config)),
    ]

_scalafmt_config = rule(
    attrs = {
        "config": attr.label(
            allow_single_file = [".conf"],
            default = "//:.scalafmt.conf",
            doc = "The Scalafmt configuration file.",
        ),
    },
    implementation = _scalafmt_config_impl,
)

def register_scalafmt_toolchain(name, config, visibility = ["//visibility:public"]):
    """Declares a Scalafmt toolchain that can be used with the rules in `@rules_scala_annex//rules:scala_with_scalafmt.bzl` or `@rules_scala_annex//rules:scalafmt.bzl`.

    See [scalafmt.md](../scalafmt.md) for more information.

    Args:
        name: The name of the toolchain.
        config: The Scalafmt configuration file.
        visibility: The visibility of the toolchain.
    """

    _scalafmt_config(
        name = "{}-configuration".format(name),
        config = config,
        visibility = visibility,
    )

    native.config_setting(
        name = "{}-setting".format(name),
        flag_values = {
            scalafmt_toolchain_setting: name,
        },
    )

    native.toolchain(
        name = name,
        target_settings = [":{}-setting".format(name)],
        toolchain = ":{}-configuration".format(name),
        toolchain_type = "@rules_scala_annex//rules/scalafmt:toolchain_type",
        visibility = visibility,
    )
