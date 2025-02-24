load("@bazel_skylib//lib:paths.bzl", "paths")
load(
    "@rules_scala_annex//rules:providers.bzl",
    _ScalaConfiguration = "ScalaConfiguration",
    _SemanticDbInfo = "SemanticDbInfo",
    _SemanticDbPhaseInfo = "SemanticDbPhaseInfo",
)

def _semanticdb_directory_from_file(file):
    """
    This is janky, but we're limited in what we can do in this function. From the
    [documentation](https://bazel.build/rules/lib/builtins/Args#add_all) on `Args#add_all`:

    To avoid unintended retention of large analysis-phase data structures into the execution phase,
    the `map_each` function must be declared by a top-level `def` statement; it may not be a
    nested function closure by default.
    """

    return file.path[:file.path.find("META-INF") - 1]

#
# PHASE: semanticdb
#
# Configures the compiler to output SemanticDB metadata. Note that this phase won't work without the
# SemanticDB compiler plugin being enabled.
#
def phase_semanticdb(ctx, g):
    toolchain = ctx.toolchains["//rules/scala:toolchain_type"]

    if toolchain.scala_configuration.semanticdb_bundle:
        return _SemanticDbPhaseInfo(outputs = [], arguments_modifier = lambda _: None)

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

        if toolchain.scala_configuration.version.startswith("2"):
            arguments.add("--compiler_option=-P:semanticdb:failures:error")
            arguments.add("--compiler_option_referencing_path=-P:semanticdb:sourceroot:${workDir}")
            arguments.add_all(
                [outputs[0]],
                format_each = "--compiler_option_referencing_path=-P:semanticdb:targetroot:${path} %s",
                map_each = _semanticdb_directory_from_file,
            )
        else:
            arguments.add_all(
                [outputs[0]],
                format_each = "--compiler_option_referencing_path=-semanticdb-target:${path} %s",
                map_each = _semanticdb_directory_from_file,
            )

            arguments.add("--compiler_option_referencing_path=-sourceroot:${workDir}")
            arguments.add("--compiler_option=-Ysemanticdb")

    g.out.providers.append(
        _SemanticDbInfo(
            target_root = "{}/{}".format(ctx.label.package, directory_name),
            semanticdb_files = outputs,
        ),
    )

    return _SemanticDbPhaseInfo(outputs = outputs, arguments_modifier = add_scalacopts)
