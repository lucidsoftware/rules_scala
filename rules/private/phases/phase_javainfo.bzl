load(
    "@bazel_tools//tools/jdk:toolchain_utils.bzl",
    "find_java_runtime_toolchain",
    "find_java_toolchain",
)
load("@rules_java//java/common:java_common.bzl", "java_common")
load(
    "@rules_scala_annex//rules:providers.bzl",
    _ScalaConfiguration = "ScalaConfiguration",
    _ScalaInfo = "ScalaInfo",
)
load(
    "//rules/common:private/utils.bzl",
    _collect = "collect",
)

#
# PHASE: javainfo
#
# Builds up the JavaInfo provider. And the ScalaInfo, while we're at it.
# And DefaultInfo.
#

def phase_javainfo(ctx, g):
    sruntime_deps = java_common.merge(_collect(JavaInfo, ctx.attr.runtime_deps))
    sexports = java_common.merge(_collect(JavaInfo, getattr(ctx.attr, "exports", [])))
    scala_configuration_runtime_deps = _collect(JavaInfo, g.init.scala_configuration.runtime_classpath)

    if len(ctx.attr.srcs) == 0 and len(ctx.attr.resources) == 0:
        java_info = java_common.merge([g.classpaths.sdeps, sexports, sruntime_deps])
    else:
        compile_jar = ctx.outputs.jar
        if (ctx.toolchains["//rules/scala:toolchain_type"].scala_configuration.use_ijar):
            compile_jar = java_common.run_ijar(
                ctx.actions,
                jar = ctx.outputs.jar,
                target_label = ctx.label,

                # See https://bazel.build/extending/config#accessing-attributes-with-transitions:
                # "When attaching a transition to an outgoing edge (regardless of whether the
                # transition is a 1:1 or 1:2+ transition), `ctx.attr` is forced to be a list if it
                # isn't already. The order of elements in this list is unspecified."
                java_toolchain = find_java_toolchain(ctx, ctx.attr._java_toolchain[0]),
            )

        source_jar_name = ctx.outputs.jar.basename.replace(".jar", "-src.jar")
        output_source_jar = ctx.actions.declare_file(
            source_jar_name,
            sibling = ctx.outputs.jar,
        )

        source_jar = java_common.pack_sources(
            ctx.actions,
            output_source_jar = output_source_jar,
            sources = ctx.files.srcs,
            java_toolchain = find_java_toolchain(ctx, ctx.attr._java_toolchain[0]),
        )

        java_info = JavaInfo(
            compile_jar = compile_jar,
            neverlink = getattr(ctx.attr, "neverlink", False),
            output_jar = ctx.outputs.jar,
            source_jar = source_jar,
            exports = [sexports],
            runtime_deps = [sruntime_deps] + scala_configuration_runtime_deps,
            deps = [g.classpaths.sdeps],
        )

    scala_info = _ScalaInfo(
        macro = getattr(ctx.attr, "macro", False),
        scala_configuration = g.init.scala_configuration,
    )

    output_group_info = OutputGroupInfo(
        **g.out.output_groups
    )

    g.out.providers.extend([
        output_group_info,
        java_info,
        scala_info,
    ])

    return struct(
        java_info = java_info,
        output_group_info = output_group_info,
        scala_info = scala_info,
    )
