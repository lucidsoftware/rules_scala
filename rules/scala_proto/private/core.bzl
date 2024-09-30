load(
    "//rules/common:private/utils.bzl",
    _resolve_execution_reqs = "resolve_execution_reqs",
    _safe_name = "safe_name",
)

scala_proto_library_private_attributes = {}

def scala_proto_library_implementation(ctx):
    proto_deps = [dep for dep in ctx.attr.deps if ProtoInfo in dep]
    if proto_deps != ctx.attr.deps:
        fail("disallowed non proto deps in %s" % ctx.attr.deps)

    protos = [dep[ProtoInfo] for dep in proto_deps]

    transitive_sources = depset(transitive = [proto.transitive_sources for proto in protos])
    transitive_proto_path = depset(transitive = [proto.transitive_proto_path for proto in protos])

    compiler = ctx.toolchains["@rules_scala_annex//rules/scala_proto:compiler_toolchain_type"]

    compiler_inputs, _ = ctx.resolve_tools(tools = [compiler.compiler])

    srcjar = ctx.outputs.srcjar

    gendir_base_path = "tmp"
    gendir = ctx.actions.declare_directory(
        gendir_base_path + "/" + _safe_name(ctx.attr.name),
    )

    args = ctx.actions.args()
    args.add_all("--output_dir", [gendir], expand_directories = False)
    args.add_all("--proto_paths", transitive_proto_path)
    if ctx.attr.grpc:
        args.add("--grpc")
    args.add_all("--", transitive_sources)
    args.set_param_file_format("multiline")
    args.use_param_file("@%s", use_always = True)

    if compiler.compiler_supports_workers:
        supports_workers = "1"
    else:
        supports_workers = "0"

    ctx.actions.run(
        mnemonic = "ScalaProtoCompile",
        inputs = depset(direct = [], transitive = [transitive_sources]),
        outputs = [gendir],
        executable = compiler.compiler.files_to_run,
        tools = compiler_inputs,
        progress_message = "Compiling %{label} protobuf into Scala source",
        execution_requirements = _resolve_execution_reqs(
            ctx,
            {
                "supports-multiplex-workers": supports_workers,
                "supports-workers": supports_workers,
                "supports-multiplex-sandboxing": supports_workers,
                "supports-worker-cancellation": supports_workers,
                "supports-path-mapping": supports_workers,
            },
        ),
        arguments = [args],
    )

    shell_args = ctx.actions.args()
    shell_args.add(ctx.executable._zipper)
    shell_args.add_all([gendir], expand_directories = False)
    shell_args.add(gendir.short_path)
    shell_args.add(srcjar)

    ctx.actions.run_shell(
        inputs = [gendir],
        outputs = [srcjar],
        arguments = [shell_args],
        command = """$1 c $4 META-INF/= $(find -L $2 -type f | while read v; do echo ${v#"${2%$3}"}=$v; done)""",
        progress_message = "Bundling compiled Scala into srcjar for %{label}",
        tools = [ctx.executable._zipper],
        execution_requirements = _resolve_execution_reqs(ctx, {
            "supports-path-mapping": "1",
        }),
    )

    return [OutputGroupInfo(
        sources_dir = depset([gendir]),
    )]
