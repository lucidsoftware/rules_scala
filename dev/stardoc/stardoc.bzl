load("@bazel_skylib//lib:paths.bzl", "paths")
load(
    "@rules_scala_annex//rules:providers.bzl",
    _DocInfo = "DocInfo",
)

_bzl_files_containing_rules = [
    "//rules:scala.bzl",
    "//rules:scala_proto.bzl",
    "//rules:scala_with_scalafmt.bzl",
    "//rules:scalafmt.bzl",
    "//rules/scalafmt:register_toolchain.bzl",
]

def _get_stardoc_targets():
    result = []

    for label in _bzl_files_containing_rules:
        sanitized_name = label.removeprefix("//rules:").removeprefix("//rules/").replace(":", "_")

        result.append(
            _DocInfo(
                name = paths.replace_extension(sanitized_name, "-docs"),
                input = label,
                out = paths.replace_extension(sanitized_name, ".md"),
            ),
        )

    return result

stardoc_targets = _get_stardoc_targets()
