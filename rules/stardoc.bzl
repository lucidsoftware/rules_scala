load("@bazel_skylib//lib:paths.bzl", "paths")

_bzl_files_containing_rules = [
    "rules_scala.bzl",
    "scala.bzl",
    "scala_proto.bzl",
    "scala_with_scalafmt.bzl",
    "scalafmt.bzl",
    "//rules/scala:workspace_2_12.bzl",
    "//rules/scala:workspace.bzl",
    "//rules/scala_proto:workspace.bzl",
    "//rules/scalafmt:workspace.bzl",
]

def _get_stardoc_targets():
    result = []

    for label in _bzl_files_containing_rules:
        sanitized_name = label.removeprefix("//rules/").replace(":", "_")

        result.append(
            struct(
                name = paths.replace_extension(sanitized_name, "-docs"),
                input = label,
                out = paths.replace_extension(sanitized_name, ".md"),
            ),
        )

    return result

stardoc_targets = _get_stardoc_targets()
