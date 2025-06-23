load("@rules_java//java:defs.bzl", "JavaInfo")
load("@rules_java//java/common:java_common.bzl", "java_common")
load("//rules:providers.bzl", "NativeProfileInfo")

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
    java_std_classpath = java_common.merge(
        [target[JavaInfo] for target in native_configuration.java_std_classpath],
    ).transitive_compile_time_jars

    inputs = depset(
        ctx.files.srcs,
        transitive = [g.classpaths.compile, g.classpaths.compiler, java_std_classpath],
    )

    outputs = [g.classpaths.jar] + g.semanticdb.outputs
    compiler_classpath = g.classpaths.compiler.to_list() + java_std_classpath.to_list()

    # These need to be separate because they're processed before the main method is invoked and can't be included in the
    # arguments file
    system_properties = ctx.actions.args()
    system_properties.add_joined(
        compiler_classpath,
        format_joined = "-Dscala.boot.class.path=%s",
        join_with = ":",
    )

    if native_configuration.pgo:
        profile_file = ctx.actions.declare_file("{}.iprof".format(ctx.label.name))

        system_properties.add(profile_file, format = "-XX:ProfilesDumpFile=%s")
        outputs.append(profile_file)
        g.out.providers.append(NativeProfileInfo(profile_file = profile_file))

    arguments = ctx.actions.args()
    arguments.add_joined("-classpath", g.classpaths.compile, join_with = ":")
    arguments.add("-d", g.classpaths.jar)
    arguments.add_all(toolchain.scala_configuration.global_scalacopts)
    arguments.add_all(ctx.attr.scalacopts)
    arguments.add_all(g.classpaths.srcs)
    arguments.set_param_file_format("multiline")
    arguments.use_param_file("@%s", use_always = True)

    ctx.actions.run(
        arguments = [system_properties, arguments],
        executable = native_configuration.native_scalac.files_to_run,
        execution_requirements = {
            "supports-multiplex-workers": "1",
            "supports-worker-cancellation": "1",
            "supports-workers": "1",
        },
        inputs = inputs,
        mnemonic = "NativeScalaCompile",
        outputs = outputs,
        progress_message = "Native Compiling Scala %{label}",
        toolchain = "@rules_scala_annex//rules/scala:toolchain_type",
    )

    return struct(
        mains_file = None,
    )
