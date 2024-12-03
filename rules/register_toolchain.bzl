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

original_scala_toolchain_setting = "@rules_scala_annex//rules/scala:original-scala-toolchain"
scala_toolchain_setting = "@rules_scala_annex//rules/scala:scala-toolchain"

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
        "incremental": attr.bool(default = False),
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

register_bootstrap_toolchain = _make_register_toolchain(_bootstrap_configuration)
register_zinc_toolchain = _make_register_toolchain(_zinc_configuration)

def _scala_toolchain_incoming_transition_impl(settings, attr):
    # We set `original_scala_toolchain_setting` so we can reset the toolchain to its original value
    # in `scala_toolchain_outgoing_transition`. That way, we can ensure every target is built under
    # a single toolchain, thus preventing duplicate builds.
    #
    # This is inspired by what the rules_go folks are doing.
    return {} if attr.scala_toolchain_name == "" else {
        original_scala_toolchain_setting: settings[scala_toolchain_setting],
        scala_toolchain_setting: attr.scala_toolchain_name,
    }

scala_toolchain_incoming_transition = transition(
    implementation = _scala_toolchain_incoming_transition_impl,
    inputs = [scala_toolchain_setting],
    outputs = [original_scala_toolchain_setting, scala_toolchain_setting],
)

def _scala_toolchain_outgoing_transition_impl(settings, _):
    original_scala_toolchain = settings[original_scala_toolchain_setting]

    return {} if original_scala_toolchain == "" else {
        # Although `original_scala_toolchain_setting` will be overridden in the incoming transition,
        # we set it to "" so that non-Scala targets aren't built under different values of this
        # setting. That way, they aren't built multiple times.
        original_scala_toolchain_setting: "",
        scala_toolchain_setting: original_scala_toolchain,
    }

scala_toolchain_outgoing_transition = transition(
    implementation = _scala_toolchain_outgoing_transition_impl,
    inputs = [original_scala_toolchain_setting],
    outputs = [original_scala_toolchain_setting, scala_toolchain_setting],
)

scala_toolchain_attributes = {
    "scala_toolchain_name": attr.string(
        doc = "The name of the Scala toolchain to use for this target (as provided to `register_*_toolchain`)",
    ),
}
