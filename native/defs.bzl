load("@rules_graalvm//graalvm:defs.bzl", "native_image")
load("@rules_java//java:defs.bzl", "JavaInfo", "java_common")
load("@rules_java//java/common:java_semantics.bzl", "semantics")
load("//rules:register_toolchain.bzl", "scala_toolchain_setting")
load("//rules:scala.bzl", "scala_binary")

def _generate_build_java_std_jars_argument(module_output):
    return "{}:{}".format(module_output[0], module_output[1].path)

def _java_std_jars_impl(ctx):
    java_runtime = ctx.toolchains[semantics.JAVA_RUNTIME_TOOLCHAIN_TYPE].java_runtime
    outputs = [
        ctx.actions.declare_file("{}-{}.jar".format(ctx.label.name, module))
        for module in ctx.attr.modules
    ]

    arguments = ctx.actions.args()
    arguments.add(java_runtime.java_home)
    arguments.add_all(zip(ctx.attr.modules, outputs), map_each = _generate_build_java_std_jars_argument)

    ctx.actions.run(
        arguments = [arguments],
        executable = ctx.executable._build_java_std_jars,
        execution_requirements = {
            "supports-path-mapping": "1",
        },
        inputs = java_runtime.files,
        outputs = outputs,
        mnemonic = "BuildJavaStdJars",
    )

    return [
        DefaultInfo(files = depset(outputs)),
        java_common.merge([
            JavaInfo(
                output_jar = output,
                compile_jar = output,
            )
            for output in outputs
        ]),
    ]

java_std_jars = rule(
    attrs = {
        "_build_java_std_jars": attr.label(
            allow_single_file = True,
            cfg = "exec",
            executable = True,
            default = Label("//native:build_java_std_jars.sh"),
        ),
        "modules": attr.string_list(
            doc = "The Java platform modules (e.g. `java.base`) to generate JARs for. Defaults to none.",
            default = [],
        ),
    },
    implementation = _java_std_jars_impl,
    provides = [JavaInfo],
    toolchains = [semantics.JAVA_RUNTIME_TOOLCHAIN_TYPE],
)

def _native_scalac_macros_transition_impl(settings, attr):
    result = dict(settings)

    if attr.scala_toolchain_name != "":
        result[scala_toolchain_setting] = attr.scala_toolchain_name

    return result

_native_scalac_macros_transition = transition(
    implementation = _native_scalac_macros_transition_impl,
    inputs = [],
    outputs = [scala_toolchain_setting],
)

def _native_scalac_macros_impl(ctx):
    return [java_common.merge([target[JavaInfo] for target in ctx.attr.deps])]

native_scalac_macros = rule(
    attrs = {
        "deps": attr.label_list(
            cfg = _native_scalac_macros_transition,
            providers = [JavaInfo],
        ),
        "scala_toolchain_name": attr.string(
            doc = """\
The name of the Scala toolchain to compile the macros with.

We recommend choosing a different toolchain than the one this `native_scalac_macros` is being used in, to prevent
dependency cycles.""",
        ),
    },
    implementation = _native_scalac_macros_impl,
    provides = [JavaInfo],
)

def native_scalac(name, compiler_classpath, macros = None, reflection_configuration = None, **kwargs):
    scala_binary(
        name = "{}-wrapper".format(name),
        srcs = ["@rules_scala_annex//src/main/scala/higherkindness/rules_scala/native:CompilerWrapper.scala"],
        scala_toolchain_name = "annex_zinc_2_13",
        deps = compiler_classpath + [
            "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/error",
            "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/worker",
        ],
    )

    native_image(
        name = name,
        main_class = "higherkindness.rules_scala.native.CompilerWrapper",
        # TODO: Implement reflection configuration merging at either the rules_scala_annex level or rules_graalvm level
        #
        # See this issue for more information:
        # https://github.com/sgammon/rules_graalvm/issues/248
        reflection_configuration = "@rules_scala_annex//third_party/scalac-native/scalac-substitutions:reflection-config.json" if reflection_configuration == None else reflection_configuration,
        deps = ["{}-wrapper".format(name)] + ([] if macros == None else [macros]) + [
            "@rules_scala_annex//third_party/scalac-native/scalac-substitutions:scalac-java-substitutions-no-dependencies",
            "@rules_scala_annex//third_party/scalac-native/scalac-substitutions:scalac-scala-substitutions-no-dependencies",
        ],
        **kwargs
    )
