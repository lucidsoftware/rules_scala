#
# PHASE: bare_compile
#
# An alternative compile phase that uses no-frills, bare-bones compilation worker that invokes the Scala 2 compiler
# directly. This phase (and the toolchain to which it belongs) are only designed to be used for testing, so it doesn't
# support features like:
# - Scala 3 compilation
# - Compiler plugins
# - Source JARs
# - Dependency checking
# - Main class detection
# - Test discovery
#

def phase_bare_compile(ctx, g):
    if g.classpaths.plugin:
        fail("plugins aren't yet supported by the bare toolchain.")

    if g.classpaths.src_jars:
        fail("source JARs aren't yet supported by the bare toolchain.")

    toolchain = ctx.toolchains["//rules/scala:toolchain_type"]
    bare_configuration = toolchain.bare_configuration
    inputs = depset(ctx.files.srcs, transitive = [g.classpaths.compile, g.classpaths.compiler])
    outputs = [g.classpaths.jar] + g.semanticdb.outputs
    compiler_classpath = g.classpaths.compiler.to_list()
    arguments = ctx.actions.args()
    arguments.add_joined("-classpath", g.classpaths.compile, join_with = ":")
    arguments.add("-d", g.classpaths.jar)
    arguments.add_all(toolchain.scala_configuration.global_scalacopts)
    arguments.add_all(ctx.attr.scalacopts)
    arguments.add_all(g.classpaths.srcs)
    arguments.set_param_file_format("multiline")
    arguments.use_param_file("@%s", use_always = True)

    ctx.actions.run(
        arguments = [arguments],
        executable = bare_configuration.worker.files_to_run,
        execution_requirements = {
            "supports-multiplex-workers": "1",
            "supports-worker-cancellation": "1",
            "supports-workers": "1",
        },
        inputs = inputs,
        mnemonic = "ScalaCompile",
        outputs = outputs,
        progress_message = "Bare Compiling Scala %{label}",
        toolchain = "@rules_scala_annex//rules/scala:toolchain_type",
    )

    return struct(mains_file = None)
