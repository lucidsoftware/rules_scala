load("@rules_java//java/common:java_info.bzl", "JavaInfo")
load(
    "//rules:providers.bzl",
    "CodeCoverageConfiguration",
    "DepsConfiguration",
    "ScalaConfiguration",
    "ScalaRulePhase",
    "ZincConfiguration",
)
load(
    "//rules/private:phases.bzl",
    "phase_bootstrap_compile",
    "phase_coverage_jacoco",
    "phase_zinc_compile",
    "phase_zinc_depscheck",
)
load("//rules/private:transitions.bzl", "scala_toolchain_setting")

def _bootstrap_configuration_impl(ctx):
    return [
        platform_common.ToolchainInfo(
            scala_configuration = ScalaConfiguration(
                compiler_classpath = ctx.attr.compiler_classpath,
                global_plugins = ctx.attr.global_plugins,
                global_scalacopts = ctx.attr.global_scalacopts,
                runtime_classpath = ctx.attr.runtime_classpath,
                semanticdb_bundle = ctx.attr.semanticdb_bundle,
                version = ctx.attr.version,
                use_ijar = ctx.attr.use_ijar,
            ),
            scala_rule_phases = ScalaRulePhase(
                phases = [
                    ("+", "semanticdb", "compile", phase_bootstrap_compile),
                ],
            ),
        ),
    ]

_bootstrap_configuration = rule(
    attrs = {
        "compiler_classpath": attr.label_list(
            doc = "JVM targets that will always be on the compiler classpath. Usually, this is the compiler itself and the standard library.",
            mandatory = True,
            providers = [JavaInfo],
        ),
        "global_plugins": attr.label_list(
            doc = "scalac plugins that will always be enabled.",
            providers = [JavaInfo],
        ),
        "global_scalacopts": attr.string_list(
            doc = "scalac options that will always be enabled.",
        ),
        "runtime_classpath": attr.label_list(
            doc = "JVM targets that will always be on the runtime classpath. Usually, this is the standard library.",
            mandatory = True,
            providers = [JavaInfo],
        ),
        "semanticdb_bundle": attr.bool(
            default = True,
            doc = "Whether to bundle SemanticDB files in the resulting JAR. Note that in Scala 2, this requires the SemanticDB compiler plugin.",
        ),
        "use_ijar": attr.bool(
            doc = "Whether to use ijar for this compiler. See https://github.com/bazelbuild/bazel/blob/master/third_party/ijar/README.txt for more information.",
            default = True,
        ),
        "version": attr.string(
            doc = "The Scala version this compiler corresponds to.",
            mandatory = True,
        ),
    },
    implementation = _bootstrap_configuration_impl,
)

def _zinc_configuration_impl(ctx):
    return [
        platform_common.ToolchainInfo(
            scala_configuration = ScalaConfiguration(
                compiler_classpath = ctx.attr.compiler_classpath,
                global_plugins = ctx.attr.global_plugins,
                global_scalacopts = ctx.attr.global_scalacopts,
                runtime_classpath = ctx.attr.runtime_classpath,
                semanticdb_bundle = ctx.attr.semanticdb_bundle,
                use_ijar = ctx.attr.use_ijar,
                version = ctx.attr.version,
            ),
            zinc_configuration = ZincConfiguration(
                compile_worker = ctx.attr._compile_worker,
                compiler_bridge = ctx.file.compiler_bridge,
                log_level = ctx.attr.log_level,
            ),
            deps_configuration = DepsConfiguration(
                direct = ctx.attr.deps_direct,
                used = ctx.attr.deps_used,
                worker = ctx.attr._deps_worker,
            ),
            code_coverage_configuration = CodeCoverageConfiguration(
                instrumentation_worker = ctx.attr._code_coverage_instrumentation_worker,
            ),
            scala_rule_phases = ScalaRulePhase(
                phases = [
                    ("+", "semanticdb", "compile", phase_zinc_compile),
                    ("+", "semanticdb", "depscheck", phase_zinc_depscheck),
                    ("+", "singlejar", "coverage", phase_coverage_jacoco),
                ],
            ),
        ),
    ]

_zinc_configuration_underlying = rule(
    attrs = {
        "compiler_bridge": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "compiler_classpath": attr.label_list(
            mandatory = True,
            providers = [JavaInfo],
        ),
        "deps_direct": attr.string(
            default = "error",
            doc = """Whether to perform direct dependency checking.
error: Require that directly used libraries must be declared as dependencies.
off: Don't perform direct dependency checking.""",
        ),
        "deps_used": attr.string(
            default = "error",
            doc = """Whether to perform unused dependency checking.
error: Require that all declared dependencies are used.
off: Don't perform unused dependency checking.""",
        ),
        "global_plugins": attr.label_list(providers = [JavaInfo]),
        "global_scalacopts": attr.string_list(),
        "log_level": attr.string(
            default = "warn",
            values = ["error", "warn", "info", "debug", "none"],
        ),
        "runtime_classpath": attr.label_list(
            mandatory = True,
            providers = [JavaInfo],
        ),
        "semanticdb_bundle": attr.bool(default = True),
        "use_ijar": attr.bool(default = True),
        "version": attr.string(mandatory = True),
        "_code_coverage_instrumentation_worker": attr.label(
            default = "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/workers/jacoco/instrumenter",
            allow_files = True,
            executable = True,
            cfg = "exec",
        ),
        "_compile_worker": attr.label(
            default = "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/workers/zinc/compile",
            allow_files = True,
            executable = True,
            cfg = "exec",
        ),
        "_deps_worker": attr.label(
            default = "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/workers/deps",
            allow_files = True,
            executable = True,
            cfg = "exec",
        ),
    },
    implementation = _zinc_configuration_impl,
)

def _zinc_configuration(**kwargs):
    if "deps_direct" not in kwargs:
        kwargs["deps_direct"] = select({
            "@rules_scala_annex//src/main/scala:deps_direct_off": "off",
            "//conditions:default": "error",
        })

    if "deps_used" not in kwargs:
        kwargs["deps_used"] = select({
            "@rules_scala_annex//src/main/scala:deps_used_off": "off",
            "//conditions:default": "error",
        })

    _zinc_configuration_underlying(**kwargs)

def _make_register_toolchain(configuration_rule):
    def result(name, visibility = ["//visibility:public"], **kwargs):
        configuration_rule(
            name = "{}-configuration".format(name),
            visibility = visibility,
            **kwargs
        )

        native.config_setting(
            name = "{}-setting".format(name),
            flag_values = {
                scala_toolchain_setting: name,
            },
        )

        native.toolchain(
            name = name,
            target_settings = [":{}-setting".format(name)],
            toolchain = ":{}-configuration".format(name),
            toolchain_type = "@rules_scala_annex//rules/scala:toolchain_type",
            visibility = visibility,
        )

    return result

def register_bare_toolchain(name, compiler_classpath, **kwargs):
    scala_binary(
        name = "{}-worker".format(name),
        srcs = ["@rules_scala_annex//src/main/scala/higherkindness/rules_scala/workers/bare:bare-srcs"],
        scala_toolchain_name = "annex_zinc_2_13",
        deps = [
            "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/error",
            "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/worker",
        ] + compiler_classpath,
    )

    underlying = _make_register_toolchain(_bare_configuration)
    underlying(
        name = name,
        compiler_classpath = compiler_classpath,
        worker = ":{}-worker".format(name),
        **kwargs
    )

register_bootstrap_toolchain = _make_register_toolchain(_bootstrap_configuration)
register_zinc_toolchain = _make_register_toolchain(_zinc_configuration)
