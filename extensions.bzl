"""
Module extension for configuring the default Scala version.
"""

load("@bazel_features//:features.bzl", "bazel_features")

def _config_repo_impl(repository_ctx):
    repository_ctx.file("BUILD.bazel", """\
load("@bazel_skylib//rules:common_settings.bzl", "string_flag", "string_setting")

string_flag(
    name = "scala-version",
    build_setting_default = {default_scala_version},
    scope = "universal",
    visibility = ["//visibility:public"],
)

string_setting(
    name = "original-scala-version",
    build_setting_default = "",
    visibility = ["//visibility:public"],
)
""".format(
        default_scala_version = repr(repository_ctx.attr.default_scala_version),
    ))

    if bazel_features.external_deps.repo_metadata_has_reproducible:
        return repository_ctx.repo_metadata(reproducible = True)
    else:
        return None

_config_repo = repository_rule(
    implementation = _config_repo_impl,
    attrs = {
        "default_scala_version": attr.string(mandatory = True),
    },
)

def _scala_impl(module_ctx):
    default_scala_version = ""

    # Find the defaults set by the root module
    for module in module_ctx.modules:
        if module.is_root:
            if len(module.tags.defaults) > 1:
                fail("scala.defaults() may only be called once per MODULE.bazel")
            for defaults in module.tags.defaults:
                default_scala_version = defaults.scala_version

    # For any defaults not set by the root module, get them from rules_scala_annex
    if not default_scala_version:
        for module in module_ctx.modules:
            if module.name == "rules_scala_annex":
                if len(module.tags.defaults) > 1:
                    fail("scala.defaults() may only be called once per MODULE.bazel")
                for defaults in module.tags.defaults:
                    default_scala_version = defaults.scala_version

    if not default_scala_version:
        fail("No default Scala version set. Call scala.defaults(scala_version = \"...\") in your MODULE.bazel.")

    _config_repo(
        name = "rules_scala_annex_config",
        default_scala_version = default_scala_version,
    )

    if bazel_features.external_deps.extension_metadata_has_reproducible:
        return module_ctx.extension_metadata(reproducible = True)
    else:
        return None

_defaults_tag = tag_class(
    attrs = {
        "scala_version": attr.string(
            doc = "The default Scala version (e.g., '3', '2.13', '3.3.7').",
            mandatory = True,
        ),
    },
)

scala = module_extension(
    implementation = _scala_impl,
    tag_classes = {"defaults": _defaults_tag},
)
