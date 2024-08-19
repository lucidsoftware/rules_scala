load("@bazel_skylib//lib:paths.bzl", "paths")
load("@rules_scala_annex//rules:providers.bzl", _ScalaConfiguration = "ScalaConfiguration")

#
# PHASE: semanticdb
#
# Configures the compiler to output SemanticDB metadata. Note that this phase won't work without the
# SemanticDB compiler plugin being enabled.
#
def phase_semanticdb(ctx, g):
    scala_configuration = ctx.attr.scala[_ScalaConfiguration]

    if scala_configuration.semanticdb_bundle:
        return struct(outputs = [], scalacopts = [])

    outputs = []
    semanticdb_directory = paths.join("_semanticdb/", ctx.label.name)
    semanticdb_target_root = paths.join(paths.dirname(ctx.outputs.jar.path), semanticdb_directory)

    for source in ctx.files.srcs:
        if source.extension == "scala":
            output_filename = paths.join(
                semanticdb_directory,
                "META-INF", "semanticdb",
                "{}.semanticdb".format(source.path),
            )

            outputs.append(ctx.actions.declare_file(output_filename))

    if scala_configuration.version.startswith("2"):
        scalacopts = [
            "-P:semanticdb:failures:error",
            "-P:semanticdb:targetroot:{}".format(semanticdb_target_root),
        ]
    else:
        scalacopts = [
            "-semanticdb-target:{}".format(semanticdb_target_root),
            "-Ysemanticdb",
        ]

    return struct(outputs = outputs, scalacopts = scalacopts)
