##
## top level rules
##

load("@bazel_skylib//lib:dicts.bzl", _dicts = "dicts")
load("@rules_java//java/common:java_common.bzl", "java_common")
load("@rules_java//java/common:java_info.bzl", "JavaInfo")
load(
    "//rules:intellij_aspect_compat.bzl",
    _intellij_aspect_compat_attributes = "intellij_aspect_compat_attributes",
)
load(
    "//rules/private:coverage_replacements_provider.bzl",
    _coverage_replacements_provider = "coverage_replacements_provider",
)
load(
    "//rules/private:phases.bzl",
    _adjust_phases = "adjust_phases",
    _phase_binary_deployjar = "phase_binary_deployjar",
    _phase_binary_launcher = "phase_binary_launcher",
    _phase_classpaths = "phase_classpaths",
    _phase_coda = "phase_coda",
    _phase_coverage_jacoco = "phase_coverage_jacoco",
    _phase_ijinfo = "phase_ijinfo",
    _phase_javainfo = "phase_javainfo",
    _phase_labeledjars = "phase_labeledjars",
    _phase_library_defaultinfo = "phase_library_defaultinfo",
    _phase_outputgroupinfo = "phase_outputgroupinfo",
    _phase_resources = "phase_resources",
    _phase_semanticdb = "phase_semanticdb",
    _phase_singlejar = "phase_singlejar",
    _phase_test_launcher = "phase_test_launcher",
    _run_phases = "run_phases",
)
load(
    "//rules/scala:private/doc.bzl",
    _scaladoc_implementation = "scaladoc_implementation",
    _scaladoc_private_attributes = "scaladoc_private_attributes",
)
load(
    "//rules/scala:private/import.bzl",
    _scala_import_implementation = "scala_import_implementation",
    _scala_import_private_attributes = "scala_import_private_attributes",
)
load(
    "//rules/scala:private/repl.bzl",
    _scala_repl_implementation = "scala_repl_implementation",
)
load(":jvm.bzl", _labeled_jars = "labeled_jars")
load(":providers.bzl", _ScalaRulePhase = "ScalaRulePhase")
load(
    ":register_toolchain.bzl",
    _scala_incoming_transition = "scala_incoming_transition",
    _scala_outgoing_transition = "scala_outgoing_transition",
    _scala_toolchain_attributes = "scala_toolchain_attributes",
)

_compile_private_attributes = {
    "_java_toolchain": attr.label(
        cfg = _scala_outgoing_transition,
        default = Label("@bazel_tools//tools/jdk:current_java_toolchain"),
    ),
    "_singlejar": attr.label(
        cfg = "exec",
        default = "@rules_java//toolchains:singlejar",
        executable = True,
    ),

    # TODO: push java and jar_creator into a provider for the
    # bootstrap compile phase
    "_jdk": attr.label(
        default = Label("@bazel_tools//tools/jdk:current_java_runtime"),
        providers = [java_common.JavaRuntimeInfo],
        cfg = "exec",
    ),
    "_jar_creator": attr.label(
        default = Label("@rules_scala_annex//third_party/bazel/src/java_tools/buildjar/java/com/google/devtools/build/buildjar/jarhelper:jarcreator_bin"),
        executable = True,
        cfg = "exec",
    ),
}

_deps_checker_label_attributes = {
    "deps_checker_label": attr.string(
        doc = """\
The label to identify this target in the output of the dependency checker.

By default, this is just the label of the target. But sometimes—for example, when overriding an artifact with
`rules_jvm_external` to point to your own, or defining an alias to target—you want the dependency checker to suggest
you add or remove a different label as a dependency. In that case, you can set this attribute to that label.
""",
    ),
}

_compile_attributes = _deps_checker_label_attributes | {
    "srcs": attr.label_list(
        cfg = _scala_outgoing_transition,
        doc = "The source Scala and Java files (and `-sources.jar` `.srcjar` `-src.jar` files of those).",
        allow_files = [
            ".scala",
            ".java",
            ".srcjar",
            "-sources.jar",
            "-src.jar",
        ],
        flags = ["DIRECT_COMPILE_TIME_INPUT"],
        mandatory = True,
    ),
    "data": attr.label_list(
        cfg = _scala_outgoing_transition,
        doc = "The additional runtime files needed by this library.",
        allow_files = True,
    ),
    "deps": attr.label_list(
        cfg = _scala_outgoing_transition,
        aspects = [
            _labeled_jars,
            _coverage_replacements_provider.aspect,
        ],
        doc = "The JVM library dependencies.",
        providers = [JavaInfo],
    ),
    "deps_used_whitelist": attr.label_list(
        cfg = _scala_outgoing_transition,
        doc = "The JVM library dependencies to always consider used for `scala_deps_used` checks.",
        providers = [JavaInfo],
    ),
    "deps_unused_whitelist": attr.label_list(
        cfg = _scala_outgoing_transition,
        doc = "The JVM library dependencies to always consider unused for `scala_deps_direct` checks.",
        providers = [JavaInfo],
    ),
    "runtime_deps": attr.label_list(
        cfg = _scala_outgoing_transition,
        doc = "The JVM runtime-only library dependencies.",
        providers = [JavaInfo],
    ),
    "javacopts": attr.string_list(
        doc = "The Javac options.",
    ),
    "plugins": attr.label_list(
        cfg = _scala_outgoing_transition,
        doc = "The Scalac plugins.",
        providers = [JavaInfo],
    ),
    "resource_strip_prefix": attr.string(
        doc = "The path prefix to strip from classpath resources.",
    ),
    "resources": attr.label_list(
        allow_files = True,
        cfg = _scala_outgoing_transition,
        doc = "The files to include as classpath resources.",
    ),
    "resource_jars": attr.label_list(
        allow_files = [".jar"],
        cfg = _scala_outgoing_transition,
        doc = "The JARs to merge into the output JAR.",
    ),
    "scalacopts": attr.string_list(
        doc = "The Scalac options.",
    ),
}

_library_attributes = {
    "exports": attr.label_list(
        aspects = [
            _coverage_replacements_provider.aspect,
        ],
        cfg = _scala_outgoing_transition,
        doc = "The JVM libraries to add as dependencies to any libraries dependent on this one.",
        providers = [JavaInfo],
    ),
    "macro": attr.bool(
        default = False,
        doc = "Whether this library provides macros.",
    ),
    "neverlink": attr.bool(
        default = False,
        doc = "Whether this library should be excluded at runtime.",
    ),
}

_runtime_attributes = {
    "jvm_flags": attr.string_list(
        doc = "The JVM runtime flags.",
    ),
    "runtime_deps": attr.label_list(
        cfg = _scala_outgoing_transition,
        doc = "The JVM runtime-only library dependencies.",
        providers = [JavaInfo],
    ),
}

_runtime_private_attributes = {
    "_target_jdk": attr.label(
        cfg = _scala_outgoing_transition,
        default = Label("@bazel_tools//tools/jdk:current_java_runtime"),
        providers = [java_common.JavaRuntimeInfo],
    ),
    "_java_stub_template": attr.label(
        cfg = _scala_outgoing_transition,
        default = Label("//third_party/java_stub_template:java_stub_template.txt"),
        allow_single_file = True,
    ),
}

_testing_private_attributes = {
    # Mandated by Bazel, with values set according to the java rules
    # in https://github.com/bazelbuild/bazel/blob/0.22.0/src/main/java/com/google/devtools/build/lib/bazel/rules/java/BazelJavaTestRule.java#L69-L76
    "_jacocorunner": attr.label(
        default = Label("@bazel_tools//tools/jdk:JacocoCoverage"),
        cfg = "exec",
    ),
    "_lcov_merger": attr.label(
        default = Label("@bazel_tools//tools/test/CoverageOutputGenerator/java/com/google/devtools/coverageoutputgenerator:Main"),
        cfg = "exec",
    ),
}

def _extras_attributes(extras):
    return {
        "_phase_providers": attr.label_list(
            default = [pp for extra in extras for pp in extra.get("phase_providers", [])],
            providers = [_ScalaRulePhase],
        ),
    }

def _scala_library_implementation(ctx):
    return _run_phases(ctx, [
        ("resources", _phase_resources),
        ("classpaths", _phase_classpaths),
        ("javainfo", _phase_javainfo),
        ("labeledjars", _phase_labeledjars),
        ("semanticdb", _phase_semanticdb),
        ("singlejar", _phase_singlejar),
        ("coverage", _phase_coverage_jacoco),
        ("ijinfo", _phase_ijinfo),
        ("library_defaultinfo", _phase_library_defaultinfo),
        ("outputgroupinfo", _phase_outputgroupinfo),
        ("coda", _phase_coda),
    ]).coda

def _scala_binary_implementation(ctx):
    return _run_phases(ctx, [
        ("resources", _phase_resources),
        ("classpaths", _phase_classpaths),
        ("javainfo", _phase_javainfo),
        ("labeledjars", _phase_labeledjars),
        ("semanticdb", _phase_semanticdb),
        ("singlejar", _phase_singlejar),
        ("coverage", _phase_coverage_jacoco),
        ("ijinfo", _phase_ijinfo),
        ("binary_deployjar", _phase_binary_deployjar),
        ("binary_launcher", _phase_binary_launcher),
        ("outputgroupinfo", _phase_outputgroupinfo),
        ("coda", _phase_coda),
    ]).coda

def _scala_test_implementation(ctx):
    return _run_phases(ctx, [
        ("resources", _phase_resources),
        ("classpaths", _phase_classpaths),
        ("javainfo", _phase_javainfo),
        ("labeledjars", _phase_labeledjars),
        ("semanticdb", _phase_semanticdb),
        ("singlejar", _phase_singlejar),
        ("coverage", _phase_coverage_jacoco),
        ("ijinfo", _phase_ijinfo),
        ("test_launcher", _phase_test_launcher),
        ("outputgroupinfo", _phase_outputgroupinfo),
        ("coda", _phase_coda),
    ]).coda

def make_scala_library(*extras):
    return rule(
        attrs = _dicts.add(
            _compile_attributes,
            _compile_private_attributes,
            _library_attributes,
            _scala_toolchain_attributes,
            _intellij_aspect_compat_attributes,
            _extras_attributes(extras),
            *[extra.get("attrs", {}) for extra in extras]
        ),
        cfg = _scala_incoming_transition,
        doc = "Compiles a Scala JVM library.",
        implementation = _scala_library_implementation,
        outputs = _dicts.add(
            {
                "jar": "%{name}.jar",
            },
            *[extra.get("outputs", {}) for extra in extras]
        ),
        toolchains = [
            "//rules/scala:toolchain_type",
            "//rules/scalafmt:toolchain_type",
            "@bazel_tools//tools/jdk:toolchain_type",
        ] + [toolchain for extra in extras for toolchain in extra.get("toolchains", [])],
    )

scala_library = make_scala_library()

def make_scala_binary(*extras):
    return rule(
        attrs = _dicts.add(
            _compile_attributes,
            _compile_private_attributes,
            _runtime_attributes,
            _runtime_private_attributes,
            _scala_toolchain_attributes,
            _intellij_aspect_compat_attributes,
            {
                "main_class": attr.string(
                    doc = "The main class. If not provided, it will be inferred by its type signature.",
                ),
            },
            _extras_attributes(extras),
            *[extra.get("attrs", {}) for extra in extras]
        ),
        cfg = _scala_incoming_transition,
        doc = """
Compiles and links a Scala JVM executable.

Produces the following implicit outputs:

  - `<name>_deploy.jar`: a single jar that contains all the necessary information to run the program
  - `<name>.jar`: a jar file that contains the class files produced from the sources
  - `<name>-bin`: the script that's used to run the program in conjunction with the generated runfiles

To run the program: `bazel run <target>`
""",
        executable = True,
        implementation = _scala_binary_implementation,
        outputs = _dicts.add(
            {
                "bin": "%{name}-bin",
                "jar": "%{name}.jar",
                "deploy_jar": "%{name}_deploy.jar",
            },
            *[extra.get("outputs", {}) for extra in extras]
        ),
        toolchains = [
            "//rules/scala:toolchain_type",
            "//rules/scalafmt:toolchain_type",
            "@bazel_tools//tools/jdk:toolchain_type",
        ] + [toolchain for extra in extras for toolchain in extra.get("toolchains", [])],
    )

scala_binary = make_scala_binary()

def make_scala_test(*extras):
    return rule(
        attrs = _dicts.add(
            _compile_attributes,
            _compile_private_attributes,
            _runtime_attributes,
            _runtime_private_attributes,
            _scala_toolchain_attributes,
            _intellij_aspect_compat_attributes,
            _testing_private_attributes,
            {
                "isolation": attr.string(
                    default = "none",
                    doc = "The isolation level to apply",
                    values = [
                        "classloader",
                        "none",
                        "process",
                    ],
                ),
                "scalacopts": attr.string_list(doc = "Options to pass to scalac."),
                "shared_deps": attr.label_list(
                    cfg = _scala_outgoing_transition,
                    doc = "If isolation is \"classloader\", the list of deps to keep loaded between tests",
                    providers = [JavaInfo],
                ),
                "frameworks": attr.string_list(
                    default = [
                        "org.scalatest.tools.Framework",
                        "org.scalacheck.ScalaCheckFramework",
                        "org.specs2.runner.Specs2Framework",
                        "minitest.runner.Framework",
                        "utest.runner.Framework",
                        "com.novocode.junit.JUnitFramework",
                    ],
                    doc = "The list of test frameworks to check for. These should conform to the sbt test interface (https://github.com/sbt/test-interface).",
                ),
                "runner": attr.label(
                    cfg = _scala_outgoing_transition,
                    default = "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/workers/zinc/test",
                ),
                "sequential": attr.bool(
                    default = False,
                    doc = "Whether to run test classes sequentially. If false, they'll be run concurrently.",
                ),
                "subprocess_runner": attr.label(
                    cfg = _scala_outgoing_transition,
                    default = "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/sbt-testing:subprocess",
                ),
            },
            _extras_attributes(extras),
            *[extra.get("attrs", {}) for extra in extras]
        ),
        cfg = _scala_incoming_transition,
        doc = """
Compiles and links a collection of Scala tests.

To buid and run all tests: `bazel test <target>`

To build and run a specific test: `bazel test <target> --test_filter=<filter_expression>`
<br>(Note: the syntax of the `<filter_expression>` varies by test framework, and not all test frameworks support the `test_filter` option at this time.)

[More Info](/docs/scala.md#tests)
""",
        executable = True,
        implementation = _scala_test_implementation,
        outputs = _dicts.add(
            {
                "bin": "%{name}-bin",
                "jar": "%{name}.jar",
            },
            *[extra.get("outputs", {}) for extra in extras]
        ),
        test = True,
        toolchains = [
            "//rules/scala:toolchain_type",
            "//rules/scalafmt:toolchain_type",
            "@bazel_tools//tools/jdk:toolchain_type",
        ] + [toolchain for extra in extras for toolchain in extra.get("toolchains", [])],
    )

scala_test = make_scala_test()

# scala_repl

_scala_repl_private_attributes = _dicts.add(
    _runtime_private_attributes,
    {
        "_runner": attr.label(
            cfg = "target",
            executable = True,
            default = "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/workers/zinc/repl",
        ),
    },
)

scala_repl = rule(
    attrs = _dicts.add(
        _scala_repl_private_attributes,
        _scala_toolchain_attributes,
        _intellij_aspect_compat_attributes,
        {
            "data": attr.label_list(
                cfg = _scala_outgoing_transition,
                doc = "The additional runtime files needed by this REPL.",
                allow_files = True,
            ),
            "deps": attr.label_list(
                cfg = _scala_outgoing_transition,
                doc = "Dependencies that should be made available to the REPL.",
                providers = [JavaInfo],
            ),
            "jvm_flags": attr.string_list(
                doc = "The JVM runtime flags.",
            ),
            "scalacopts": attr.string_list(doc = "Options to pass to scalac."),
        },
    ),
    cfg = _scala_incoming_transition,
    doc = """
Launches a REPL with all given dependencies available.

To run: `bazel run <target>`
""",
    executable = True,
    implementation = _scala_repl_implementation,
    outputs = {
        "bin": "%{name}-bin",
    },
    toolchains = [
        "//rules/scala:toolchain_type",
        "@bazel_tools//tools/jdk:toolchain_type",
    ],
)

scala_import = rule(
    attrs = _dicts.add(
        _scala_import_private_attributes,
        _deps_checker_label_attributes,
        {
            "deps": attr.label_list(
                doc = "Libraries used by this one.",
                providers = [JavaInfo],
            ),
            "exports": attr.label_list(
                doc = "Libraries made available by this one. See https://bazel.build/versions/6.0.0/reference/be/java#java_library.exports.",
                providers = [JavaInfo],
            ),
            "jars": attr.label_list(
                allow_files = True,
                doc = "JAR files to include in this library.",
            ),
            "neverlink": attr.bool(
                default = False,
                doc = "Set this to True to exclude this library from the runtime classpath (i.e. if it should only be used at compile-time).",
            ),
            "runtime_deps": attr.label_list(
                doc = "Libraries used by this one, but which aren't referenced explicitly and need only be available at runtime.",
                providers = [JavaInfo],
            ),
            "srcjar": attr.label(
                allow_single_file = True,
                doc = "The source JAR for this library.",
            ),
        },
    ),
    doc = """
Creates a Scala JVM library.

Use this only for libraries with macros. Otherwise, use `java_import`.""",
    toolchains = ["@bazel_tools//tools/jdk:toolchain_type"],
    implementation = _scala_import_implementation,
)

scaladoc = rule(
    attrs = _dicts.add(
        _scala_toolchain_attributes,
        _intellij_aspect_compat_attributes,
        _scaladoc_private_attributes,
        {
            "compiler_deps": attr.label_list(
                cfg = _scala_outgoing_transition,
                doc = "JVM targets that should be included on the compile classpath.",
                providers = [JavaInfo],
            ),
            "deps": attr.label_list(
                cfg = _scala_outgoing_transition,
                doc = "Dependencies that should be made available to the Scaladoc tool. These may include libraries referenced in Scaladoc or public signatures.",
                providers = [JavaInfo],
            ),
            "srcs": attr.label_list(
                allow_files = [
                    ".java",
                    ".scala",
                    ".srcjar",
                    "-sources.jar",
                    "-src.jar",
                ],
                cfg = _scala_outgoing_transition,
                doc = "Sources from which to generate Scaladoc. These may include `*.java` files, `*.scala` files, and source JARs.",
            ),
            "scalacopts": attr.string_list(doc = "Options to pass to scalac."),
            "title": attr.string(doc = "The name of the project. If none is provided, the target label will be used."),
        },
    ),
    cfg = _scala_incoming_transition,
    doc = "Generates Scaladoc.",
    implementation = _scaladoc_implementation,
    toolchains = [
        "//rules/scala:toolchain_type",
        "@bazel_tools//tools/jdk:toolchain_type",
    ],
)
