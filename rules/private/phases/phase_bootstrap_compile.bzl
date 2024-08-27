load(
    "//rules/common:private/utils.bzl",
    _resolve_execution_reqs = "resolve_execution_reqs",
    _strip_margin = "strip_margin",
)

#
# PHASE: bootstrap compile
#
# An alternative compile phase that shells out to scalac directly
#

def phase_bootstrap_compile(ctx, g):
    if g.classpaths.plugin:
        fail("plugins aren't supported for bootstrap_scala rules")
    if g.classpaths.src_jars:
        fail("source jars supported for bootstrap_scala rules")

    inputs = depset(
        ctx.files.srcs,
        transitive = [ctx.attr._jdk[java_common.JavaRuntimeInfo].files, g.classpaths.compile, g.classpaths.compiler],
    )

    tmp = ctx.actions.declare_directory("{}/tmp/classes".format(ctx.label.name))

    compiler_classpath = ":".join([f.path for f in g.classpaths.compiler.to_list()])
    compile_classpath = ":".join([f.path for f in g.classpaths.compile.to_list()])
    srcs = " ".join([f.path for f in g.classpaths.srcs])

    scala_configuration = g.javainfo.scala_info.scala_configuration

    main_class = "scala.tools.nsc.Main"
    if int(scala_configuration.version[0]) >= 3:
        main_class = "dotty.tools.dotc.Main"

    command = _strip_margin(
        """
            |set -eo pipefail
            |
            |{java} \\
            |  -cp {compiler_classpath} \\
            |  {main_class} \\
            |  -cp {compile_classpath} \\
            |  -d {tmp} \\
            |  {global_scalacopts} \\
            |  {scalacopts} \\
            |  {srcs}
            |
            |{jar_creator} {output_jar} {tmp} 2> /dev/null
            |""".format(
            compiler_classpath = compiler_classpath,
            compile_classpath = compile_classpath,
            global_scalacopts = " ".join(scala_configuration.global_scalacopts),
            java = ctx.attr._jdk[java_common.JavaRuntimeInfo].java_executable_exec_path,
            jar_creator = ctx.executable._jar_creator.path,
            main_class = main_class,
            output_jar = g.classpaths.jar.path,
            scalacopts = " ".join(ctx.attr.scalacopts),
            srcs = srcs,
            tmp = tmp.path,
        ),
    )

    ctx.actions.run_shell(
        inputs = inputs,
        tools = [ctx.executable._jar_creator],
        mnemonic = "BootstrapScalacompile",
        outputs = [g.classpaths.jar, tmp],
        command = command,
        execution_requirements = _resolve_execution_reqs(ctx, {}),
    )
