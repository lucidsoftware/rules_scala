load(
    "@rules_scala_annex//rules:providers.bzl",
    _CodeCoverageConfiguration = "CodeCoverageConfiguration",
)
load(
    "@rules_scala_annex//rules/common:private/utils.bzl",
    _resolve_execution_reqs = "resolve_execution_reqs",
)
load(
    "@rules_scala_annex//rules/private:coverage_replacements_provider.bzl",
    _coverage_replacements_provider = "coverage_replacements_provider",
)

def phase_coverage_jacoco(ctx, g):
    if not ctx.configuration.coverage_enabled:
        return

    toolchain = ctx.toolchains["//rules/scala:toolchain_type"]
    worker_inputs, _ = ctx.resolve_tools(
        tools = [toolchain.code_coverage_configuration.instrumentation_worker],
    )

    args = ctx.actions.args()

    instrumented_output_jar = ctx.actions.declare_file(
        "{}-offline.jar".format(ctx.outputs.jar.basename.split(".")[0]),
    )
    in_out_pairs = [
        (ctx.outputs.jar, instrumented_output_jar),
    ]

    args.add_all(in_out_pairs, map_each = _format_in_out_pairs)

    args.set_param_file_format("multiline")
    args.use_param_file("@%s", use_always = True)
    ctx.actions.run(
        mnemonic = "JacocoInstrumenter",
        inputs = [in_out_pair[0] for in_out_pair in in_out_pairs] + worker_inputs.to_list(),
        outputs = [in_out_pair[1] for in_out_pair in in_out_pairs],
        executable = toolchain.code_coverage_configuration.instrumentation_worker.files_to_run,
        execution_requirements = _resolve_execution_reqs(
            ctx,
            {
                "supports-multiplex-workers": "1",
                "supports-workers": "1",
                "supports-multiplex-sandboxing": "1",
                "supports-worker-cancellation": "1",
            },
        ),
        arguments = [args],
    )

    replacements = {i: o for (i, o) in in_out_pairs}

    g.out.providers.extend([
        _coverage_replacements_provider.create(
            replacements = replacements,
        ),
    ])

    return struct(
        instrumented_files = struct(
            dependency_attributes = _coverage_replacements_provider.dependency_attributes,
            extensions = ["scala", "java"],
            source_attributes = ["srcs"],
        ),
        replacements = replacements,
    )

def _format_in_out_pairs(in_out_pair):
    return (["--jar", "%s=%s" % (in_out_pair[0].path, in_out_pair[1].path)])
