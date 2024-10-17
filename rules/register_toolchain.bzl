load("@rules_scala_annex_scala_toolchain//:default.bzl", "default_scala_toolchain_name")
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
    "phase_semanticdb",
    "phase_zinc_compile",
    "phase_zinc_depscheck",
)

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
                    ("+", "javainfo", "compile", phase_bootstrap_compile),
                ],
            ),
        ),
    ]

_bootstrap_configuration = rule(
    attrs = {
        "compiler_classpath": attr.label_list(
            mandatory = True,
            providers = [JavaInfo],
        ),
        "global_plugins": attr.label_list(
            doc = "Scalac plugins that will always be enabled.",
            providers = [JavaInfo],
        ),
        "global_scalacopts": attr.string_list(
            doc = "Scalac options that will always be enabled.",
        ),
        "semanticdb_bundle": attr.bool(
            default = True,
            doc = "Whether to bundle SemanticDB files in the resulting JAR. Note that in Scala 2, this requires the SemanticDB compiler plugin.",
        ),
        "use_ijar": attr.bool(
            default = True,
            doc = "Whether to use ijars for this compiler.",
        ),
        "runtime_classpath": attr.label_list(
            mandatory = True,
            providers = [JavaInfo],
        ),
        "version": attr.string(mandatory = True),
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
                incremental = ctx.attr.incremental,
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
                    ("+", "javainfo", "semanticdb", phase_semanticdb),
                    ("+", "javainfo", "compile", phase_zinc_compile),
                    ("+", "javainfo", "depscheck", phase_zinc_depscheck),
                    ("+", "singlejar", "coverage", phase_coverage_jacoco),
                ],
            ),
        ),
    ]

_zinc_configuration_underlying = rule(
    attrs = {
        "compiler_classpath": attr.label_list(
            mandatory = True,
            providers = [JavaInfo],
        ),
        "compiler_bridge": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "deps_direct": attr.string(default = "error"),
        "deps_used": attr.string(default = "error"),
        "global_plugins": attr.label_list(
            doc = "Scalac plugins that will always be enabled.",
            providers = [JavaInfo],
        ),
        "global_scalacopts": attr.string_list(doc = "Scalac options that will always be enabled."),
        "incremental": attr.bool(
            default = False,
            doc = "Whether Zinc's incremental compilation will be available for this Zinc compiler. If True, this requires additional configuration to use incremental compilation.",
        ),
        "log_level": attr.string(
            default = "warn",
            doc = "Compiler log level",
        ),
        "runtime_classpath": attr.label_list(
            mandatory = True,
            providers = [JavaInfo],
        ),
        "semanticdb_bundle": attr.bool(
            default = True,
            doc = "Whether to bundle SemanticDB files in the resulting JAR. Note that in Scala 2, this requires the SemanticDB compiler plugin.",
        ),
        "use_ijar": attr.bool(
            default = True,
            doc = "Whether to use ijars for this compiler.",
        ),
        "version": attr.string(mandatory = True),
        "_code_coverage_instrumentation_worker": attr.label(
            allow_files = True,
            cfg = "exec",
            default = "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/workers/jacoco/instrumenter",
            executable = True,
        ),
        "_compile_worker": attr.label(
            allow_files = True,
            cfg = "exec",
            default = "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/workers/zinc/compile",
            executable = True,
        ),
        "_deps_worker": attr.label(
            allow_files = True,
            cfg = "exec",
            default = "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/workers/deps",
            executable = True,
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
                "@rules_scala_annex_scala_toolchain//:scala-toolchain": name,
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

register_bootstrap_toolchain = _make_register_toolchain(_bootstrap_configuration)
register_zinc_toolchain = _make_register_toolchain(_zinc_configuration)

def _scala_toolchain_incoming_transition_impl(settings, attr):
    if attr.scala_toolchain_name == "":
        return {}

    return {
        "@rules_scala_annex_scala_toolchain//:scala-toolchain": attr.scala_toolchain_name,
    }

scala_toolchain_incoming_transition = transition(
    implementation = _scala_toolchain_incoming_transition_impl,
    inputs = ["@rules_scala_annex_scala_toolchain//:scala-toolchain"],
    outputs = ["@rules_scala_annex_scala_toolchain//:scala-toolchain"],
)

def _scala_toolchain_outgoing_transition_impl(_1, _2):
    return {
        "@rules_scala_annex_scala_toolchain//:scala-toolchain": default_scala_toolchain_name,
    }

scala_toolchain_outgoing_transition = transition(
    implementation = _scala_toolchain_outgoing_transition_impl,
    inputs = [],
    outputs = ["@rules_scala_annex_scala_toolchain//:scala-toolchain"],
)

scala_toolchain_attributes = {
    "scala_toolchain_name": attr.string(
        doc = "The name of the Scala toolchain to use for this target (as provided to `register_*_toolchain`)",
    ),
}
