load(
    "@rules_scala_annex//rules/common:private/utils.bzl",
    _resolve_execution_reqs = "resolve_execution_reqs",
    _short_path = "short_path",
)

scala_format_attributes = {
    "config": attr.label(
        allow_single_file = [".conf"],
        default = "@scalafmt_default//:config",
        doc = "The Scalafmt configuration file.",
    ),
    "_fmt": attr.label(
        cfg = "exec",
        default = "@rules_scala_annex//rules/scalafmt",
        executable = True,
    ),
    "_runner": attr.label(
        allow_single_file = True,
        default = "@rules_scala_annex//rules/scalafmt:runner",
    ),
}

scala_non_default_format_attributes = {
    "_testrunner": attr.label(
        allow_single_file = True,
        default = "@rules_scala_annex//rules/scalafmt:testrunner",
    ),
    "format": attr.bool(default = True),
}

def build_format(ctx):
    files = []
    runner_inputs, _ = ctx.resolve_tools(tools = [ctx.attr._fmt])
    manifest_content = []
    for src in ctx.files.srcs:
        if src.short_path.endswith(".scala") and src.is_source:
            file = ctx.actions.declare_file(src.short_path)
            files.append(file)
            args = ctx.actions.args()
            args.add("--config")
            args.add(ctx.file.config)
            args.add(src)
            args.add(file)
            args.set_param_file_format("multiline")
            args.use_param_file("@%s", use_always = True)
            ctx.actions.run(
                arguments = ["--jvm_flag=-Dfile.encoding=UTF-8", args],
                executable = ctx.executable._fmt,
                outputs = [file],
                inputs = [ctx.file.config, src],
                tools = runner_inputs,
                execution_requirements = _resolve_execution_reqs(
                    ctx,
                    {
                        "supports-multiplex-workers": "1",
                        "supports-workers": "1",
                        "supports-multiplex-sandboxing": "1",
                        "supports-worker-cancellation": "1",
                        "supports-path-mapping": "1",
                    },
                ),
                mnemonic = "ScalaFmt",
            )
            manifest_content.append("{} {}".format(src.short_path, file.short_path))

    manifest = ctx.actions.declare_file("{}/manifest.txt".format(ctx.label.name))
    ctx.actions.write(manifest, "\n".join(manifest_content) + "\n")

    return manifest, files

def format_runner(ctx, manifest, files):
    args = ctx.actions.args()
    args.add(ctx.file._runner)
    args.add(ctx.workspace_name)
    args.add(manifest.short_path)
    args.add(ctx.outputs.scalafmt_runner)

    ctx.actions.run_shell(
        inputs = [ctx.file._runner, manifest] + files,
        outputs = [ctx.outputs.scalafmt_runner],
        command = "cat $1 | sed -e s#%workspace%#$2# -e s#%manifest%#$3# > $4",
        arguments = [args],
        execution_requirements = _resolve_execution_reqs(ctx, {
            "supports-path-mapping": "1",
        }),
        mnemonic = "CreateScalaFmtRunner",
    )

def format_tester(ctx, manifest, files):
    args = ctx.actions.args()
    args.add(ctx.file._testrunner)
    args.add(ctx.workspace_name)
    args.add(manifest.short_path)
    args.add(ctx.outputs.scalafmt_testrunner)

    ctx.actions.run_shell(
        inputs = [ctx.file._testrunner, manifest] + files,
        outputs = [ctx.outputs.scalafmt_testrunner],
        command = "cat $1 | sed -e s#%workspace%#$2# -e s#%manifest%#$3# > $4",
        arguments = [args],
        execution_requirements = _resolve_execution_reqs(ctx, {
            "supports-path-mapping": "1",
        }),
        mnemonic = "CreateScalaFmtTester",
    )

def scala_format_test_implementation(ctx):
    manifest, files = build_format(ctx)
    format_runner(ctx, manifest, files)

    return DefaultInfo(
        executable = ctx.outputs.scalafmt_runner,
        files = depset([ctx.outputs.scalafmt_runner, manifest] + files),
        runfiles = ctx.runfiles(files = [manifest] + files + ctx.files.srcs),
    )
