load("@rules_java//java/common:java_common.bzl", "java_common")
load(
    "//rules/common:private/utils.bzl",
    _write_launcher = "write_launcher",
)

#
# PHASE: binary_launcher
#
# Writes a Scala binary launcher
#

def phase_binary_launcher(ctx, g):
    inputs = ctx.files.data

    if ctx.attr.main_class != "":
        main_class = ctx.attr.main_class
        mains_file = None
    else:
        main_class = None
        mains_file = g.compile.mains_file
        inputs = inputs + [mains_file]

    files = _write_launcher(
        ctx,
        "{}/".format(ctx.label.name),
        ctx.outputs.bin,
        g.javainfo.java_info.transitive_runtime_jars,
        jvm_flags = [ctx.expand_location(f, ctx.attr.data) for f in ctx.attr.jvm_flags],
        main_class = main_class,
        mains_file = mains_file,
    )

    g.out.providers.append(DefaultInfo(
        executable = ctx.outputs.bin,
        files = depset([ctx.outputs.bin, ctx.outputs.jar] + g.semanticdb.outputs),
        runfiles = ctx.runfiles(
            files = inputs + files,
            transitive_files = depset(
                order = "default",

                # See https://bazel.build/extending/config#accessing-attributes-with-transitions:
                # "When attaching a transition to an outgoing edge (regardless of whether the
                # transition is a 1:1 or 1:2+ transition), `ctx.attr` is forced to be a list if it
                # isn't already. The order of elements in this list is unspecified."
                transitive = [ctx.attr._target_jdk[0][java_common.JavaRuntimeInfo].files, g.javainfo.java_info.transitive_runtime_jars],
            ),
            collect_default = True,
        ),
    ))
