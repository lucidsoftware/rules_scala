load("@rules_java//java/common:java_common.bzl", "java_common")

#
# PHASE: native_compile
#
# An alternative compile phase that uses a native image of scalac compiled with
# [GraalVM](https://www.graalvm.org/).
#

def phase_native_compile(ctx, g):
    if g.classpaths.plugin:
        fail("plugins aren't yet supported for targets compiled with a native scalac.")

    if g.classpaths.src_jars:
        fail("source JARs aren't yet supported for targets compiled with a native scalac.")

    toolchain = ctx.toolchains["//rules/scala:toolchain_type"]
    native_configuration = toolchain.native_configuration
    java_runtime_info = native_configuration.java_8[java_common.JavaRuntimeInfo]
    inputs = depset(
        ctx.files.srcs,
        transitive = [java_runtime_info.files, g.classpaths.compile, g.classpaths.compiler],
    )

    compiler_classpath = g.classpaths.compiler.to_list()
    compiler_classpath.append("{}/jre/lib/rt.jar".format(java_runtime_info.java_home))

    arguments = ctx.actions.args()
    arguments.add_joined(
        compiler_classpath,
        format_joined = "-Dscala.boot.class.path=%s",
        join_with = ":",
    )

    arguments.add_joined("-classpath", g.classpaths.compile, join_with = ":")
    arguments.add("-d", g.classpaths.jar)
    arguments.add_all(toolchain.scala_configuration.global_scalacopts)
    arguments.add_all(ctx.attr.scalacopts)
    arguments.add_all(g.classpaths.srcs)

    ctx.actions.run(
        arguments = [arguments],
        executable = native_configuration.native_scalac.files_to_run,
        inputs = inputs,
        mnemonic = "ScalaCompile",
        outputs = [g.classpaths.jar] + g.semanticdb.outputs,
        progress_message = "Compiling Scala %{label}",
        toolchain = "@rules_scala_annex//rules/scala:toolchain_type",
    )

    return struct(
        mains_file = None,
    )
