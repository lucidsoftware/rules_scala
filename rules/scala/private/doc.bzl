load(
    "@rules_scala_annex//rules:providers.bzl",
    _ScalaConfiguration = "ScalaConfiguration",
    _ZincConfiguration = "ZincConfiguration",
)
load(
    "//rules/common:private/utils.bzl",
    _collect = "collect",
    _resolve_execution_reqs = "resolve_execution_reqs",
    _separate_src_jars = "separate_src_jars",
)

scaladoc_private_attributes = {
    "_runner": attr.label(
        cfg = "host",
        executable = True,
        default = "//src/main/scala/higherkindness/rules_scala/workers/zinc/doc",
    ),
}

def scaladoc_implementation(ctx):
    toolchain = ctx.toolchains["//rules/scala:toolchain_type"]
    scompiler_classpath = java_common.merge(
        _collect(JavaInfo, toolchain.scala_configuration.compiler_classpath),
    )

    html = ctx.actions.declare_directory("html")
    tmp = ctx.actions.declare_directory("tmp")

    classpath = depset(transitive = [dep[JavaInfo].transitive_compile_time_jars for dep in ctx.attr.deps])
    compiler_classpath = depset(
        transitive =
            [scompiler_classpath.transitive_runtime_jars] +
            [dep[JavaInfo].transitive_runtime_jars for dep in ctx.attr.compiler_deps],
    )

    srcs, src_jars = _separate_src_jars(ctx.files.srcs)

    scalacopts = ["-doc-title", ctx.attr.title or ctx.label] + ctx.attr.scalacopts

    args = ctx.actions.args()
    args.add("--compiler_bridge", toolchain.zinc_configuration.compiler_bridge)
    args.add_all("--compiler_classpath", compiler_classpath)
    args.add_all("--classpath", classpath)
    args.add_all(scalacopts, format_each = "--option=%s")
    args.add("--output_html", html.path)
    args.add_all("--source_jars", src_jars)
    args.add("--tmp", tmp.path)
    args.add_all("--", srcs)
    args.set_param_file_format("multiline")
    args.use_param_file("@%s", use_always = True)

    runner_inputs, _, input_manifests = ctx.resolve_command(tools = [ctx.attr._runner])

    ctx.actions.run(
        arguments = [args],
        executable = ctx.attr._runner.files_to_run.executable,
        execution_requirements = _resolve_execution_reqs(
            ctx,
            {
                "supports-multiplex-workers": "1",
                "supports-workers": "1",
                "supports-multiplex-sandboxing": "1",
                "supports-worker-cancellation": "1",
            },
        ),
        input_manifests = input_manifests,
        inputs = depset(
            src_jars + srcs + [toolchain.zinc_configuration.compiler_bridge],
            transitive = [classpath, compiler_classpath],
        ),
        mnemonic = "ScalaDoc",
        outputs = [html, tmp],
    )

    return [
        DefaultInfo(
            files = depset([html]),
        ),
    ]
