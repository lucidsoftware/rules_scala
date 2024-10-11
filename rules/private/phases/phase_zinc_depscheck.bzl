load(
    "@rules_scala_annex//rules:providers.bzl",
    _DepsConfiguration = "DepsConfiguration",
    _LabeledJars = "LabeledJars",
)
load(
    "@rules_scala_annex//rules/common:private/utils.bzl",
    _resolve_execution_reqs = "resolve_execution_reqs",
)

#
# PHASE: depscheck
# Dependencies are checked to see if they are used/unused.
# Success files are outputted if dependency checking was "successful"
# according to the configuration/options.
#

def phase_zinc_depscheck(ctx, g):
    deps_configuration = ctx.toolchains["//rules/scala:toolchain_type"].deps_configuration

    deps_checks = {}
    labeled_jar_groups = depset(transitive = [dep[_LabeledJars].values for dep in ctx.attr.deps])

    worker_inputs, _ = ctx.resolve_tools(tools = [deps_configuration.worker])
    for name in ("direct", "used"):
        deps_check = ctx.actions.declare_file("{}/depscheck_{}.success".format(ctx.label.name, name))
        deps_args = ctx.actions.args()
        deps_args.add(name, format = "--check_%s=true")
        deps_args.add_all("--direct", [dep.label for dep in ctx.attr.deps], format_each = "_%s")

        # Check the comment on the function we're calling here to understand why
        # we're not using map_each
        for labeled_jar_group in labeled_jar_groups.to_list():
            _add_args_for_depscheck_labeled_group(labeled_jar_group, deps_args)

        deps_args.add("--label", ctx.label, format = "_%s")
        deps_args.add_all("--used_whitelist", [dep.label for dep in ctx.attr.deps_used_whitelist], format_each = "_%s")
        deps_args.add_all("--unused_whitelist", [dep.label for dep in ctx.attr.deps_unused_whitelist], format_each = "_%s")
        deps_args.add("--")
        deps_args.add(g.compile.used)
        deps_args.add(deps_check)
        deps_args.set_param_file_format("multiline")
        deps_args.use_param_file("@%s", use_always = True)
        ctx.actions.run(
            mnemonic = "ScalaCheckDeps",
            inputs = [g.compile.used] + worker_inputs.to_list(),
            outputs = [deps_check],
            executable = deps_configuration.worker.files_to_run,
            execution_requirements = _resolve_execution_reqs(
                ctx,
                {
                    "supports-multiplex-workers": "1",
                    "supports-workers": "1",
                    "supports-multiplex-sandboxing": "1",
                    "supports-worker-cancellation": "1",
                },
            ),
            arguments = [deps_args],
        )
        deps_checks[name] = deps_check

    outputs = []
    if deps_configuration.direct == "error":
        outputs.append(deps_checks["direct"])
    if deps_configuration.used == "error":
        outputs.append(deps_checks["used"])

    g.out.output_groups["depscheck"] = depset(outputs)

    return struct(
        checks = deps_checks,
        outputs = outputs,
        toolchain = deps_configuration,
    )

# If you use avoid using map_each, then labels are converted to their apparent repo name rather than
# their canonical repo name. The apparent repo name is the human readable one that we want for use
# with buildozer. See https://bazel.build/rules/lib/builtins/Args.html for more info
#
# Avoiding map_each is why we've got this odd section of add and add_all to create a --group
def _add_args_for_depscheck_labeled_group(labeled_jar_group, deps_args):
    deps_args.add("--group")
    deps_args.add(labeled_jar_group.label, format = "_%s")
    deps_args.add_all(labeled_jar_group.jars)
