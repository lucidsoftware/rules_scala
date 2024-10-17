def _impl(ctx):
    foo_file = ctx.actions.declare_file("foo.txt")
    outputs = [foo_file]

    args = ctx.actions.args()
    args.add(foo_file)
    args.set_param_file_format("multiline")
    args.use_param_file("@%s", use_always = True)

    ctx.actions.run(
        outputs = outputs,
        arguments = [args],
        mnemonic = "VerbositySpecWorkerRun",
        execution_requirements = {
            "supports-multiplex-workers": "1",
            "supports-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "supports-worker-cancellation": "1",
            "supports-path-mapping": "1",
        },
        progress_message = "Running verbosity spec worker %{label}",
        executable = ctx.executable.verbosity_spec_worker,
    )

    return [
        DefaultInfo(files = depset(outputs)),
    ]

verbosity_spec_worker_run = rule(
    implementation = _impl,
    doc = "Runs a worker that prints the verbosity level it received from the work request",
    attrs = {
        "verbosity_spec_worker": attr.label(
            executable = True,
            cfg = "exec",
            allow_files = True,
            default = Label(":verbosity-spec-worker"),
        ),
    },
)
