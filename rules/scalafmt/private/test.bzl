load(
    "@rules_scala_annex//rules/common:private/utils.bzl",
    _make_jvm_flag_args = "make_jvm_flag_args",
    _short_path = "short_path",
)

scala_format_attributes = {
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
    "format": attr.bool(
        default = True,
        doc = "Whether to format the target. If this is False, the formatter and format tester will do nothing.",
    ),
}

def build_format(ctx):
    files = []
    manifest_content = []
    scalafmt_config = ctx.toolchains["//rules/scalafmt:toolchain_type"].scalafmt_config
    config = scalafmt_config.config

    jvm_flag_args = _make_jvm_flag_args(
        ctx,
        ["-Dfile.encoding=UTF-8"] + scalafmt_config.jvm_flags,
    )

    for src in ctx.files.srcs:
        if src.short_path.endswith(".scala") and src.is_source:
            file = ctx.actions.declare_file(src.short_path)
            files.append(file)
            args = ctx.actions.args()
            args.add("--config")
            args.add(config)
            args.add(src)
            args.add(file)
            args.set_param_file_format("multiline")
            args.use_param_file("@%s", use_always = True)
            ctx.actions.run(
                arguments = [jvm_flag_args, args],
                executable = ctx.executable._fmt,
                execution_requirements = {
                    "supports-multiplex-workers": "1",
                    "supports-workers": "1",
                    "supports-multiplex-sandboxing": "1",
                    "supports-worker-cancellation": "1",
                    "supports-path-mapping": "1",
                },
                inputs = [config, src],
                mnemonic = "ScalaFmt",
                progress_message = "Formatting Scala %{label}",
                outputs = [file],
                toolchain = None,
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
        arguments = [args],
        command = "cat $1 | sed -e s#%workspace%#$2# -e s#%manifest%#$3# > $4",
        execution_requirements = {
            "supports-path-mapping": "1",
        },
        inputs = [ctx.file._runner, manifest] + files,
        mnemonic = "CreateScalaFmtRunner",
        progress_message = "Creating Scalafmt runner %{label}",
        outputs = [ctx.outputs.scalafmt_runner],
        toolchain = None,
    )

def format_tester(ctx, manifest, files):
    args = ctx.actions.args()
    args.add(ctx.file._testrunner)
    args.add(ctx.workspace_name)
    args.add(manifest.short_path)
    args.add(ctx.outputs.scalafmt_testrunner)

    ctx.actions.run_shell(
        arguments = [args],
        command = "cat $1 | sed -e s#%workspace%#$2# -e s#%manifest%#$3# > $4",
        execution_requirements = {
            "supports-path-mapping": "1",
        },
        inputs = [ctx.file._testrunner, manifest] + files,
        mnemonic = "CreateScalaFmtTester",
        progress_message = "Creating Scalafmt tester %{label}",
        outputs = [ctx.outputs.scalafmt_testrunner],
        toolchain = None,
    )

def scala_format_test_implementation(ctx):
    manifest, files = build_format(ctx)
    format_runner(ctx, manifest, files)

    return DefaultInfo(
        executable = ctx.outputs.scalafmt_runner,
        files = depset([ctx.outputs.scalafmt_runner, manifest] + files),
        runfiles = ctx.runfiles(files = [manifest] + files + ctx.files.srcs),
    )
