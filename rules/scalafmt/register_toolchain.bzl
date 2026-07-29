load("@bazel_skylib//lib:selects.bzl", "selects")
load("//rules:register_toolchain.bzl", "create_version_config_settings")

_ScalafmtConfig = provider(
    fields = {
        "config": "The Scalafmt configuration file.",
        "jvm_flags": "JVM options to pass when invoking Scalafmt.",
    },
)

def _scalafmt_config_impl(ctx):
    return [
        platform_common.ToolchainInfo(
            scalafmt_config = _ScalafmtConfig(
                config = ctx.file.config,
                jvm_flags = ctx.attr.jvm_flags,
            ),
        ),
    ]

_scalafmt_config = rule(
    attrs = {
        "config": attr.label(
            allow_single_file = [".conf"],
            default = "//:.scalafmt.conf",
            doc = "The Scalafmt configuration file.",
        ),
        "jvm_flags": attr.string_list(
            doc = "JVM options to pass when invoking Scalafmt.",
        ),
    },
    implementation = _scalafmt_config_impl,
)

def register_scalafmt_toolchain(name, config, scala_versions, jvm_flags = [], visibility = ["//visibility:public"]):
    """Declares a Scalafmt toolchain that matches targets by Scala version.

    See [scalafmt.md](../../docs/scalafmt.md) for more information.

    Args:
        name: The name of the toolchain.
        config: The Scalafmt configuration file.
        scala_versions: A list of Scala version strings this toolchain is compatible with. Use the
            same full version constants as your Zinc toolchains, e.g., "2.13.16", "3.3.7", For
            prefixed variants, concatenate the prefix, e.g., "semanticdb_2.13.16".
        jvm_flags: JVM options to pass when invoking Scalafmt.
        visibility: The visibility of the toolchain.
    """

    _scalafmt_config(
        name = "{}-configuration".format(name),
        config = config,
        jvm_flags = jvm_flags,
        visibility = visibility,
    )

    all_scala_version_settings_groups = []
    for index, version in enumerate(scala_versions):
        group = create_version_config_settings(
            "{}-scala_version{}".format(name, index),
            version,
        )
        all_scala_version_settings_groups.append(":{}".format(group))

    combined_settings_group = "{}-scalafmt_version_setting".format(name)
    selects.config_setting_group(
        name = combined_settings_group,
        match_any = all_scala_version_settings_groups,
        visibility = ["//visibility:private"],
    )

    native.toolchain(
        name = name,
        target_settings = [":{}".format(combined_settings_group)],
        toolchain = ":{}-configuration".format(name),
        toolchain_type = "@rules_scala_annex//rules/scalafmt:toolchain_type",
        visibility = visibility,
    )
