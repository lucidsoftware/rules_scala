load("@rules_java//java/common:java_common.bzl", "java_common")
load("@rules_java//toolchains:toolchain_utils.bzl", "find_java_toolchain")
load(
    "@rules_scala_annex//rules:providers.bzl",
    _ScalaConfiguration = "ScalaConfiguration",
    _ZincCompilationInfo = "ZincCompilationInfo",
)
load(
    "@rules_scala_annex//rules/common:private/javac_options.bzl",
    "replace_source_target_with_release",
)
load(
    "@rules_scala_annex//rules/common:private/utils.bzl",
    _make_jvm_flag_args = "make_jvm_flag_args",
)

#
# PHASE: compile
#
# Compiles Scala sources ;)
#

def phase_zinc_compile(ctx, g):
    toolchain = ctx.toolchains["//rules/scala:toolchain_type"]
    mains_file = ctx.actions.declare_file("{}.jar.mains.txt".format(ctx.label.name))
    used = ctx.actions.declare_file("{}/deps_used.txt".format(ctx.label.name))

    javacopts = replace_source_target_with_release([
        ctx.expand_location(option, ctx.attr.data)
        for option in ctx.attr.javacopts + java_common.default_javac_opts(
            # See https://bazel.build/extending/config#accessing-attributes-with-transitions:
            # "When attaching a transition to an outgoing edge (regardless of whether the transition
            # is a 1:1 or 1:2+ transition), `ctx.attr` is forced to be a list if it isn't already.
            # The order of elements in this list is unspecified."
            java_toolchain = find_java_toolchain(ctx, ctx.attr._java_toolchain[0]),
        )
    ])

    common_scalacopts = toolchain.scala_configuration.global_scalacopts + ctx.attr.scalacopts

    args = ctx.actions.args()
    args.add("--compiler_bridge", toolchain.zinc_configuration.compiler_bridge)
    args.add_all("--compiler_classpath", g.classpaths.compiler)
    args.add_all("--classpath", g.classpaths.compile)
    args.add_all(common_scalacopts, format_each = "--compiler_option=%s")
    args.add_all(javacopts, format_each = "--java_compiler_option=%s")
    args.add(ctx.label, format = "--label=%s")
    args.add("--main_manifest", mains_file)
    args.add("--output_jar", g.classpaths.jar)
    args.add("--output_used", used)
    args.add_all("--plugins", g.classpaths.plugin)
    args.add_all("--source_jars", g.classpaths.src_jars)
    args.add("--log_level", toolchain.zinc_configuration.log_level)

    g.semanticdb.arguments_modifier(args)

    inputs = depset(
        [toolchain.zinc_configuration.compiler_bridge] + ctx.files.data + ctx.files.srcs,
        transitive = [
            g.classpaths.plugin,
            g.classpaths.compile,
            g.classpaths.compiler,
        ],
    )

    outputs = [g.classpaths.jar, mains_file, used] + g.semanticdb.outputs

    if hasattr(ctx.attr, "frameworks"):
        tests_file = ctx.actions.declare_file("{}/tests.json".format(ctx.label.name))

        args.add_all("--test_frameworks", ctx.attr.frameworks)
        args.add("--tests_file", tests_file)

        outputs.append(tests_file)
    else:
        tests_file = None

    args.add_all("--", g.classpaths.srcs)
    args.set_param_file_format("multiline")
    args.use_param_file("@%s", use_always = True)

    worker = toolchain.zinc_configuration.compile_worker

    execution_requirements_tags = {
        "supports-multiplex-workers": "1",
        "supports-workers": "1",
        "supports-multiplex-sandboxing": "1",
        "supports-worker-cancellation": "1",
        "supports-path-mapping": "1",
    }

    jvm_flag_args = _make_jvm_flag_args(ctx, toolchain.scala_configuration.jvm_flags)

    # todo: different execution path for nosrc jar?
    ctx.actions.run(
        arguments = [jvm_flag_args, args],
        executable = worker.files_to_run,
        execution_requirements = execution_requirements_tags,
        inputs = inputs,
        mnemonic = "ScalaCompile",
        outputs = outputs,
        progress_message = "Compiling Scala %{label}",
        toolchain = "@rules_scala_annex//rules/scala:toolchain_type",
    )

    jars = []
    for jar in g.javainfo.java_info.outputs.jars:
        jars.append(jar.class_jar)
        jars.append(jar.ijar)

    return _ZincCompilationInfo(
        mains_file = mains_file,
        tests_file = tests_file,
        used = used,
    )
