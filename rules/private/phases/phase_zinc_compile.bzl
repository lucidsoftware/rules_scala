load(
    "@bazel_tools//tools/jdk:toolchain_utils.bzl",
    "find_java_toolchain",
)
load(
    "@rules_scala_annex//rules:providers.bzl",
    _ScalaConfiguration = "ScalaConfiguration",
    _ZincConfiguration = "ZincConfiguration",
    _ZincInfo = "ZincInfo",
)
load(
    "@rules_scala_annex//rules/common:private/utils.bzl",
    _resolve_execution_reqs = "resolve_execution_reqs",
)

#
# PHASE: compile
#
# Compiles Scala sources ;)
#

def phase_zinc_compile(ctx, g):
    scala_configuration = ctx.attr.scala[_ScalaConfiguration]
    zinc_configuration = ctx.attr.scala[_ZincConfiguration]

    analysis_store = ctx.actions.declare_file("{}/analysis_store.gz".format(ctx.label.name))
    analysis_store_text = ctx.actions.declare_file("{}/analysis_store.text.gz".format(ctx.label.name))
    mains_file = ctx.actions.declare_file("{}.jar.mains.txt".format(ctx.label.name))
    used = ctx.actions.declare_file("{}/deps_used.txt".format(ctx.label.name))
    tmp = ctx.actions.declare_directory("{}/tmp".format(ctx.label.name))

    javacopts = [
        ctx.expand_location(option, ctx.attr.data)
        for option in ctx.attr.javacopts + java_common.default_javac_opts(
            java_toolchain = find_java_toolchain(ctx, ctx.attr._java_toolchain),
        )
    ]

    zincs = [dep[_ZincInfo] for dep in ctx.attr.deps if _ZincInfo in dep]
    common_scalacopts = ctx.attr.scalacopts + g.semanticdb.scalacopts

    args = ctx.actions.args()

    args.add_all(depset(transitive = [zinc.deps for zinc in zincs]), map_each = _compile_analysis)
    args.add("--compiler_bridge", zinc_configuration.compiler_bridge)
    args.add_all("--compiler_classpath", g.classpaths.compiler)
    args.add_all("--classpath", g.classpaths.compile)
    args.add_all(scala_configuration.global_scalacopts, format_each = "--compiler_option=%s")
    args.add_all(common_scalacopts, format_each = "--compiler_option=%s")
    args.add_all(javacopts, format_each = "--java_compiler_option=%s")
    args.add(ctx.label, format = "--label=%s")
    args.add("--main_manifest", mains_file)
    args.add("--output_analysis_store", analysis_store)
    args.add("--output_jar", g.classpaths.jar)
    args.add("--output_used", used)
    args.add_all("--plugins", g.classpaths.plugin)
    args.add_all("--source_jars", g.classpaths.src_jars)
    args.add("--tmp", tmp.path)

    args.add("--log_level", zinc_configuration.log_level)
    args.add_all("--", g.classpaths.srcs)
    args.set_param_file_format("multiline")
    args.use_param_file("@%s", use_always = True)

    worker = zinc_configuration.compile_worker

    worker_inputs, _, input_manifests = ctx.resolve_command(tools = [worker])
    inputs = depset(
        [zinc_configuration.compiler_bridge] + ctx.files.data + ctx.files.srcs + worker_inputs,
        transitive = [
            g.classpaths.plugin,
            g.classpaths.compile,
            g.classpaths.compiler,
        ] + [zinc.deps_files for zinc in zincs],
    )

    outputs = [
        g.classpaths.jar,
        mains_file,
        analysis_store,
        analysis_store_text,
        used,
        tmp,
    ] + g.semanticdb.outputs

    execution_requirements_tags = {
        "supports-multiplex-workers": "1",
        "supports-workers": "1",
    }

    # Disable sandboxing if incremental compilation features are going to be used
    # because they require stashing files outside the sandbox that Bazel isn't
    # aware of.
    if zinc_configuration.incremental:
        execution_requirements_tags["no-sandbox"] = "1"

    # todo: different execution path for nosrc jar?
    ctx.actions.run(
        mnemonic = "ScalaCompile",
        inputs = inputs,
        outputs = outputs,
        executable = worker.files_to_run.executable,
        input_manifests = input_manifests,
        execution_requirements = _resolve_execution_reqs(
            ctx,
            execution_requirements_tags,
        ),
        arguments = [args],
    )

    jars = []
    for jar in g.javainfo.java_info.outputs.jars:
        jars.append(jar.class_jar)
        jars.append(jar.ijar)
    zinc_info = _ZincInfo(
        analysis_store = analysis_store,
        deps_files = depset([analysis_store], transitive = [zinc.deps_files for zinc in zincs]),
        label = ctx.label,
        deps = depset(
            [struct(
                analysis_store = analysis_store,
                jars = tuple(jars),
                label = ctx.label,
            )],
            transitive = [zinc.deps for zinc in zincs],
        ),
    )

    g.out.providers.append(zinc_info)
    return struct(
        mains_file = mains_file,
        used = used,
        # todo: see about cleaning up & generalizing fields below
        zinc_info = zinc_info,
    )

def _compile_analysis(analysis):
    return [
        "--analysis",
        "_{}".format(analysis.label),
        analysis.analysis_store.path,
    ] + [jar.path for jar in analysis.jars]
