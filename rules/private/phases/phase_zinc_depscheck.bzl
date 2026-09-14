load(
    "@rules_scala_annex//rules:providers.bzl",
    _LabeledJars = "LabeledJars",
)
load(
    "@rules_scala_annex//rules/common:private/utils.bzl",
    _make_jvm_flag_args = "make_jvm_flag_args",
)

#
# PHASE: depscheck
# Dependencies are checked to see if they are used/unused.
# Success files are outputted if dependency checking was "successful"
# according to the configuration/options.

def _label_for_dependency_checker(target):
    if _LabeledJars in target:
        return target[_LabeledJars].label

    return target.label

def phase_zinc_depscheck(ctx, g):
    toolchain = ctx.toolchains["//rules/scala:toolchain_type"]
    deps_configuration = toolchain.deps_configuration
    labeled_jar_groups = depset(transitive = [dep[_LabeledJars].values for dep in ctx.attr.deps])
    transitive_label_objects = depset(transitive = [dep[_LabeledJars].transitive_label_objects for dep in ctx.attr.deps])
    outputs = []

    jvm_flag_args = _make_jvm_flag_args(ctx, toolchain.scala_configuration.jvm_flags)

    common_args = ctx.actions.args()

    # When a `Label` is passed to `Args#add`, Bazel formats it using the apparent repository name. But when `Label`s are
    # serialized in a `map_each` call, Bazel uses the canonical repository name. This matters because we always want to
    # use the apparent repository name when correcting dependencies.
    #
    # We *could* use `Args#add` instead of `Args#add_all` with `map_each`, but there are strong efficiency gains to be had
    # from using `Args#add_all`, so we instead serialize the `Label`s with their canonical repository names but provide a
    # *canonical-to-apparent* mapping so the worker can use the apparent repository names when suggesting corrections.
    common_args.add_all("--label_keys", transitive_label_objects, map_each = _canonical_label_key, format_each = "_%s")
    common_args.add_all("--label_names", transitive_label_objects, format_each = "_%s")
    common_args.add_all(labeled_jar_groups, map_each = _depscheck_labeled_group)
    common_args.add_all(
        "--direct",
        [_label_for_dependency_checker(dependency) for dependency in ctx.attr.deps],
        format_each = "_%s",
    )

    common_args.add("--label", ctx.label, format = "_%s")
    common_args.add_all(
        "--used_whitelist",
        [_label_for_dependency_checker(dependency) for dependency in ctx.attr.deps_used_whitelist],
        format_each = "_%s",
    )

    common_args.add_all(
        "--unused_whitelist",
        [_label_for_dependency_checker(dependency) for dependency in ctx.attr.deps_unused_whitelist],
        format_each = "_%s",
    )
    common_args.set_param_file_format("multiline")
    common_args.use_param_file("@%s", use_always = True)

    for name in ("direct", "used"):
        deps_check = ctx.actions.declare_file("{}/depscheck_{}.success".format(ctx.label.name, name))
        deps_args = ctx.actions.args()
        deps_args.add(name, format = "--check_%s=true")
        deps_args.add("--")
        deps_args.add(g.compile.used)
        deps_args.add(deps_check)
        deps_args.set_param_file_format("multiline")
        deps_args.use_param_file("@%s", use_always = True)
        ctx.actions.run(
            arguments = [jvm_flag_args, common_args, deps_args],
            executable = deps_configuration.worker.files_to_run,
            execution_requirements = {
                "supports-multiplex-workers": "1",
                "supports-workers": "1",
                "supports-multiplex-sandboxing": "1",
                "supports-worker-cancellation": "1",
                "supports-path-mapping": "1",
            },
            inputs = [g.compile.used],
            mnemonic = "ScalaCheckDeps",
            progress_message = "Checking Scala dependencies %{label}",
            outputs = [deps_check],
            toolchain = "@rules_scala_annex//rules/scala:toolchain_type",
        )

        if getattr(deps_configuration, name) == "error":
            outputs.append(deps_check)

    if "_validation" in g.out.output_groups:
        validation_transitive = [g.out.output_groups["_validation"]]
    else:
        validation_transitive = None

    g.out.output_groups["_validation"] = depset(outputs, transitive = validation_transitive)

def _canonical_label_key(label):
    return str(label)

def _depscheck_labeled_group(group):
    # The leading underscore prevents @-prefixed labels from being interpreted as argument files
    return ["--group", "_" + str(group.label)] + [jar.short_path for jar in group.jars.to_list()]
