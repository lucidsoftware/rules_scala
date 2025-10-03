load("@rules_java//java/common:java_common.bzl", "java_common")
load("@rules_java//java/common:java_info.bzl", "JavaInfo")
load("@rules_java//toolchains:toolchain_utils.bzl", "find_java_toolchain")
load("@rules_scala_annex//rules:providers.bzl", _IntellijInfo = "IntellijInfo")
load(
    "//rules/common:private/utils.bzl",
    _separate_src_jars_srcs_and_other = "separate_src_jars_srcs_and_other",
)
load("//rules/jvm:private/label.bzl", "get_labeled_jars")

scala_import_private_attributes = {
    "_java_toolchain": attr.label(
        default = Label("@bazel_tools//tools/jdk:current_java_toolchain"),
    ),
}

def scala_import_implementation(ctx):
    if ctx.files.jars:
        _src_jar, _, _jar = _separate_src_jars_srcs_and_other(ctx.files.jars)
        _src_jar += ctx.files.srcjar

        output_jar = _jar[0]

        # TODO: maybe eventually we should use this. Right now it produces
        # a warning:
        #   WARNING: Duplicate name in Manifest: <MANIFEST.MF entry>.
        #   Ensure that the manifest does not have duplicate entries, and
        #   that blank lines separate individual sections in both your
        #   manifest and in the META-INF/MANIFEST.MF entry in the jar file.
        #
        # It does this for all kinds of MANIFEST.MF entries. For example:
        #   * Implementation-Version
        #   * Implementation-Title
        #   * Implementation-URL
        # and a bunch of others
        #
        # compile_jar = java_common.stamp_jar(
        #     ctx.actions,
        #     jar = output_jar,
        #     target_label = ctx.label,
        #     java_toolchain = ctx.attr._java_toolchain,
        # )

        source_jar_name = output_jar.basename[:-len(output_jar.extension)] + "-src.jar"
        output_source_jar = ctx.actions.declare_file(source_jar_name)

        source_jar = java_common.pack_sources(
            ctx.actions,
            output_source_jar = output_source_jar,
            source_jars = _src_jar,
            java_toolchain = find_java_toolchain(ctx, ctx.attr._java_toolchain),
        )

        java_info = JavaInfo(
            output_jar = output_jar,
            compile_jar = output_jar,
            source_jar = source_jar,
            deps = [dep[JavaInfo] for dep in ctx.attr.deps],
            neverlink = ctx.attr.neverlink,
            runtime_deps = [runtime_dep[JavaInfo] for runtime_dep in ctx.attr.runtime_deps],
            exports = [export[JavaInfo] for export in ctx.attr.exports],
        )
    else:
        java_info = java_common.merge([dep[JavaInfo] for dep in ctx.attr.deps])

    providers = [java_info, create_intellij_info(ctx.label, ctx.attr.deps, java_info)]

    if ctx.attr.deps_checker_label != "":
        providers.append(get_labeled_jars(ctx.attr.deps_checker_label, java_info, ctx.attr.deps))

    return providers

def create_intellij_info(label, deps, java_info):
    # note: tried using transitive_exports from a JavaInfo that was given non-empty exports, but it was always empty
    return _IntellijInfo(
        outputs = java_info.outputs,
        transitive_exports = depset(
            [label],
            transitive = [dep[_IntellijInfo].transitive_exports for dep in deps if _IntellijInfo in dep],
        ),
    )
