load("@bazel_skylib//lib:paths.bzl", "paths")
load(
    "@rules_scala_annex//rules:providers.bzl",
    _ScalaConfiguration = "ScalaConfiguration",
    _SemanticDbInfo = "SemanticDbInfo",
    _SemanticDbPhaseInfo = "SemanticDbPhaseInfo",
)

def _semanticdb_directory_from_output_jar(file):
    """
    Using the path of the output JAR to determine the SemanticDB target root is janky, but the
    output directory won't be known at build-time, so we have to use the output JAR path as a proxy.
    It should be built under the same configuration as the SemanticDB files, since both are produced
    by the same compilation action.
    """

    return "{}/semanticdb".format(file.dirname)

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
        # Generated or external files will have the output directory (beginning with `bazel-out`) in
        # their paths, which we don't want because it isn't guaranteed to be consistent
        if (
            source.extension == "scala" and
            source.is_source and
            source.owner.repo_name == ctx.label.repo_name
        ):
            path = paths.join(
                directory_name,
                "META-INF",
                "semanticdb",
                "{}.semanticdb".format(source.path),
            )

            outputs.append(ctx.actions.declare_file(path))

    def add_scalacopts(arguments):
        output_jar = g.classpaths.jar

        if toolchain.scala_configuration.version.startswith("2"):
            arguments.add("--compiler_option=-P:semanticdb:failures:error")
            arguments.add("--compiler_option_referencing_path=-P:semanticdb:sourceroot:${workDir}")
            arguments.add_all(
                [output_jar],
                format_each = "--compiler_option_referencing_path=-P:semanticdb:targetroot:${path} %s",
                map_each = _semanticdb_directory_from_output_jar,
            )
        else:
            arguments.add_all(
                [output_jar],
                format_each = "--compiler_option_referencing_path=-semanticdb-target:${path} %s",
                map_each = _semanticdb_directory_from_output_jar,
            )

            # We don't need to change `-sourceroot` in the Scala 3 case because `ZincRunner` already takes care of
            # setting it to the work directory (so the generated TASTy files are deterministic)
            arguments.add("--compiler_option=-Ysemanticdb")

    g.out.providers.append(
        _SemanticDbInfo(
            target_root = "{}/{}".format(ctx.label.package, directory_name),
            semanticdb_files = outputs,
        ),
    )

    return _SemanticDbPhaseInfo(outputs = outputs, arguments_modifier = add_scalacopts)
