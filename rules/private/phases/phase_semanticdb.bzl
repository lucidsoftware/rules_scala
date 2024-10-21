load("@bazel_skylib//lib:paths.bzl", "paths")
load(
    "@rules_scala_annex//rules:providers.bzl",
    _ScalaConfiguration = "ScalaConfiguration",
    _SemanticDbInfo = "SemanticDbInfo",
)

def _semanticdb_directory_from_file(file):
    return file.path[:file.path.find("META-INF") - 1]

#
# PHASE: semanticdb
#
# Configures the compiler to output SemanticDB metadata. Note that this phase won't work without the
# SemanticDB compiler plugin being enabled.
#
def phase_semanticdb(ctx, g):
    scala_configuration = ctx.attr.scala[_ScalaConfiguration]

    if scala_configuration.semanticdb_bundle:
        return struct(outputs = [], arguments_modifier = lambda _: None)

    directory_name = "{}/semanticdb".format(ctx.label.name)
    outputs = []

    for source in ctx.files.srcs:
        if source.extension == "scala":
            path = paths.join(
                directory_name,
                "META-INF",
                "semanticdb",
                "{}.semanticdb".format(source.path),
            )

            outputs.append(ctx.actions.declare_file(path))

    def add_scalacopts(arguments):
        if len(outputs) == 0:
            return

        if scala_configuration.version.startswith("2"):
            arguments.add("--compiler_option=-P:semanticdb:failures:error")
            arguments.add_all(
                [outputs[0]],
                format_each = "--compiler_option=-P:semanticdb:targetroot:%s",
                map_each = _semanticdb_directory_from_file,
            )
        else:
            arguments.add_all(
                [outputs[0]],
                format_each = "--compiler_option=-semanticdb-target:%s",
                map_each = _semanticdb_directory_from_file,
            )

            arguments.add("--compiler_option=-Ysemanticdb")

    g.out.providers.append(
        _SemanticDbInfo(
            target_root = "{}/{}".format(ctx.label.package, directory_name),
            semanticdb_files = outputs,
        ),
    )

    return struct(outputs = outputs, arguments_modifier = add_scalacopts)
