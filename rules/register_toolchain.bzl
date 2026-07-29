load("@bazel_skylib//lib:selects.bzl", "selects")
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

scala_version_setting = "@rules_scala_annex_config//:scala-version"
original_scala_version_setting = "@rules_scala_annex_config//:original-scala-version"

def _bootstrap_configuration_impl(ctx):
    return [
        platform_common.ToolchainInfo(
            scala_configuration = ScalaConfiguration(
                compiler_classpath = ctx.attr.compiler_classpath,
                global_plugins = ctx.attr.global_plugins,
                global_scalacopts = ctx.attr.global_scalacopts,
                runtime_classpath = ctx.attr.runtime_classpath,
                jvm_flags = ctx.attr.jvm_flags,
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
        "jvm_flags": attr.string_list(
            doc = "JVM options to pass when invoking Scala-related actions.",
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
                jvm_flags = ctx.attr.jvm_flags,
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
        "jvm_flags": attr.string_list(
            doc = "JVM options to pass when invoking Scala-related actions.",
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

def create_version_config_settings(name, version, prefix = ""):
    """Creates hierarchical version config_settings for toolchain matching.

    For version "3.3.7" with no prefix, creates config_settings matching: "3", "3.3", "3.3.7"

    For version "3.3.7" with prefix "<prefix>", creates config_settings matching: "<prefix>_3",
    "<prefix>_3.3", "<prefix>_3.3.7"

    Args:
        name: The toolchain name, used as a prefix for config_setting target names.
        version: The Scala version, e.g., "3.3.7", "2.13.16".
        prefix: Optional prefix for scala_version to disambiguate multiple toolchains with the same
            scala_version.

    Returns:
        The name of the config_setting_group that matches any of the above.
    """
    if prefix:
        prefix = prefix + "_"

    parts = version.split(".")
    match_settings = []

    # Create a config_setting for each version prefix: major ("3"), major.minor ("3.3"),
    # major.minor.patch ("3.3.7"). Each optionally namespaced by `prefix`. The toolchain matches a
    # target whose scala_version equals any of these config settings.
    for part in range(1, len(parts) + 1):
        setting_name = "{}-match_{}".format(name, part)
        native.config_setting(
            name = setting_name,
            flag_values = {scala_version_setting: prefix + ".".join(parts[:part])},
            visibility = ["//visibility:private"],
        )
        match_settings.append(":" + setting_name)

    # Group: toolchain matches if any of the above are true
    scala_version_settings_group_name = "{}-version_setting".format(name)
    selects.config_setting_group(
        name = scala_version_settings_group_name,
        match_any = match_settings,
        visibility = ["//visibility:private"],
    )

    return scala_version_settings_group_name

def _make_register_toolchain(configuration_rule):
    def result(name, version, prefix = "", visibility = ["//visibility:public"], **kwargs):
        configuration_rule(
            name = "{}-configuration".format(name),
            visibility = visibility,
            version = version,
            **kwargs
        )

        scala_version_settings_group_name = create_version_config_settings(name, version, prefix)

        native.toolchain(
            name = name,
            target_settings = [":{}".format(scala_version_settings_group_name)],
            toolchain = ":{}-configuration".format(name),
            toolchain_type = "@rules_scala_annex//rules/scala:toolchain_type",
            visibility = visibility,
        )

    return result

register_bootstrap_toolchain = _make_register_toolchain(_bootstrap_configuration)
register_zinc_toolchain = _make_register_toolchain(_zinc_configuration)

def _scala_incoming_transition_impl(settings, attr):
    result = dict(settings)

    if attr.scala_version != "" and attr.scala_version != settings[scala_version_setting]:
        # We set `original_scala_version_setting` so we can reset the version to its
        # original value in `scala_outgoing_transition`. That way, we can ensure every target is
        # built under a single toolchain, thus preventing duplicate builds.
        #
        # We do not do this work when the version is set, but is no different than what is
        # already set. By having that check we avoid the failure mode where the original version
        # gets set equal to the current version and destroys whatever the actual original
        # version was. For example
        #  State 1:              State 2:          State 3:
        #    Setting: A      =>    Setting: B  =>    Setting: B  => Game over
        #    Original: Unset       Original: A       Original: B
        #
        # Note that the check described above should ideally not be required due to outgoing
        # transitions but it is, so something is going wrong. As a result, the check is probably
        # temporary, but who knows.
        #
        # This is inspired by what the rules_go folks are doing.
        result[original_scala_version_setting] = settings[scala_version_setting]
        result[scala_version_setting] = attr.scala_version

    return result

scala_incoming_transition = transition(
    implementation = _scala_incoming_transition_impl,
    inputs = [
        original_scala_version_setting,
        scala_version_setting,
    ],
    outputs = [
        original_scala_version_setting,
        scala_version_setting,
    ],
)

def _scala_outgoing_transition_impl(settings, _):
    result = dict(settings)
    original_scala_version = settings[original_scala_version_setting]

    # Although these original settings will be overridden in the incoming transition, we set them
    # to "" so non-Scala targets aren't built under different values of these settings. That way,
    # they aren't built multiple times.
    if original_scala_version != "":
        result[original_scala_version_setting] = ""
        result[scala_version_setting] = original_scala_version

    return result

scala_outgoing_transition = transition(
    implementation = _scala_outgoing_transition_impl,
    inputs = [
        original_scala_version_setting,
        scala_version_setting,
    ],
    outputs = [
        original_scala_version_setting,
        scala_version_setting,
    ],
)

scala_toolchain_attributes = {
    "scala_version": attr.string(
        doc = "The Scala version to use, e.g., '3', '2.13', '3.3.7', or '<prefix>_<version>' for prefixed versions.",
    ),
}
