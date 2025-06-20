ScalaConfiguration = provider(
    doc = "Scala compile-time and runtime configuration",
    fields = {
        "compiler_classpath": "The compiler classpath.",
        "global_plugins": "Globally enabled compiler plugins",
        "global_scalacopts": "Globally enabled compiler options",
        "runtime_classpath": "The runtime classpath.",
        "semanticdb_bundle": "Whether to bundle SemanticDB files in the resulting JAR. Note that in Scala 2, this requires the SemanticDB compiler plugin.",
        "use_ijar": "Whether to use ijars for this Scala compiler",
        "version": "The Scala full version.",
    },
)

ScalaInfo = provider(
    doc = "Scala library.",
    fields = {
        "macro": "whether the jar contains macros",
        "scala_configuration": "ScalaConfiguration associated with this output",
    },
)

ZincConfiguration = provider(
    doc = "Zinc configuration.",
    fields = {
        "compiler_bridge": "compiled Zinc compiler bridge",
        "compile_worker": "the worker label for compilation with Zinc",
        "log_level": "log level for the Zinc compiler",
        "incremental": "whether incremental compilation will be available for this Zinc compiler",
    },
)

DepsConfiguration = provider(
    doc = "Dependency checking configuration.",
    fields = {
        "direct": "either error or off",
        "used": "either error or off",
        "worker": "the worker label for checking used/unused deps",
    },
)

CodeCoverageConfiguration = provider(
    doc = "Code coverage related configuration",
    fields = {
        "instrumentation_worker": "the worker used for instrumenting jars",
    },
)

ScalaRulePhase = provider(
    doc = "A Scala compiler plugin",
    fields = {
        "phases": "the phases to add",
    },
)

ZincInfo = provider(
    doc = "Zinc-specific outputs.",
    fields = {
        "analysis_store": "The analysis store file.",
        "deps": "The depset of library dependency outputs.",
        "deps_files": "The depset of all Zinc files.",
        "label": "The label for this output.",
    },
)

# TODO: move these to another file?
# TODO: implement these with an aspect?

IntellijInfo = provider(
    doc = "Provider for IntelliJ.",
    fields = {
        "outputs": "java_output_jars",
        "transitive_exports": "labels of transitive dependencies",
    },
)

# TODO: compare to JavaInfo's owner
LabeledJars = provider(
    doc = "Exported jars and their labels.",
    fields = {
        "values": "The preorder depset of label and jars.",
    },
)

SemanticDbInfo = provider(
    doc = "Provided by the Scala rules when `semanticdb_bundle` is set to `False`.",
    fields = {
        "target_root": "The directory in which SemanticDB files were outputted.",
        "semanticdb_files": "The SemanticDB files.",
    },
)

CoverageReplacementsData = provider(
    doc = "Data for coverage replacements",
    fields = {
        "aspect": "Coverage replacement aspect",
        "dependency_attributes": "attributes used to form the dependency graph that we'll fold over for our aggregation",
        "combine": "Function used to combine coverage replacements",
        "from_ctx": "Function used to combine coverage replacements from a ctx",
        "create": "Provider to use for coverage replacements",
    },
)

ClasspathInfo = provider(
    doc = "Outputs from the classpath phase.",
    fields = {
        "compile": "Classpath for this compilation.",
        "compiler": "Classpath needed by the compiler for this compilation.",
        "jar": "Output jar for this compilation.",
        "plugin": "Classpath for the compiler plugins for this compilation.",
        "sdeps": "Deps. TODO: better name for this?",
        "src_jars": "Source jars for this compilation.",
        "srcs": "Source files for this compilation.",
    },
)

DocInfo = provider(
    doc = "Documentation realted info for a label.",
    fields = {
        "input": "Associated label",
        "name": "Docs label name",
        "out": "Docs file",
    },
)

LabeledJarsData = provider(
    doc = "Data for LabeledJars",
    fields = {
        "jars": "Jars associated with the label",
        "label": "Label for the jars",
    },
)

JacocoInfo = provider(
    doc = "Outputs from the Jacoco phase.",
    fields = {
        "replacements": "Coverage Replacement provider",
    },
)

JavaInfoPhaseInfo = provider(
    doc = "Outputs from the JavaInfo phase.",
    fields = {
        "java_info": "JavaInfo provider",
        "scala_info": "ScalaInfo provider",
    },
)

PhasesInfo = provider(
    doc = "Info related to the phases to run.",
)

PhasesInitInfo = provider(
    doc = "Init information needed for phases",
    fields = {
        "scala_configuration": "Scala configuration for the toolchain.",
    },
)

PhasesOutInfo = provider(
    doc = "Output related information for phases.",
    fields = {
        "output_groups": "Output groups",
        "providers": "Providers",
    },
)

ResourcesInfo = provider(
    doc = "Outputs from the resources phase",
    fields = {
        "jar": "Resource jar containing resource files",
    },
)

SemanticDbPhaseInfo = provider(
    doc = "Outputs from the SemanticDB phase.",
    fields = {
        "arguments_modifier": "Function to call to modify the scalac arguments for SemanticDB support.",
        "outputs": "Extra outputs for the SemanticDB files.",
    },
)

ZincCompilationInfo = provider(
    doc = "Outputs from the Zinc compilation phase.",
    fields = {
        "mains_file": "File containing the main methods of this compilation.",
        "used": "File containing the used deps for this compilation.",
        "zinc_info": "a ZincInfo provider for this compilation.",
    },
)

ZincDepInfo = provider(
    doc = "Information for a dep in a ZincInfo",
    fields = {
        "analysis_store": "Analysis store for this label",
        "jars": "Jars for this label",
        "label": "The label for this dep",
    },
)

NativeProfileInfo = provider(
    doc = "Contains a [Profile-Guided Optimization](https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/PGO/) profile generated by GraalVM.",
    fields = {
        "profile_file": "The generated profile file.",
    },
)

NativeConfiguration = provider(
    doc = "Contains configuration specific to native toolchains.",
    fields = {
        "java_std_classpath": """\
The unpacked Java standard library module that will be provided to scalac.
https://docs.oracle.com/en/java/javase/11/docs/api/java.base/module-summary.html

Because we're using a GraalVM-compiled scalac with this toolchain and GraalVM operates on a
[closed-world assumption](https://docs.oracle.com/en/learn/understanding-reflection-graalvm-native-image/index.html),
all classes loaded via reflection must be known at compile-time. Fortunately, scalac doesn't operate under the
assumption that it's running on the JVM and exposes a `-Dscala.boot.class.path` option that allows us to specify a
classpath from which it'll load classes like `java.lang.Object`, `java.lang.invoke.StringConcatFactory`, etc.

The only thing we need to do is generate JAR containing those base classes from the platform modules bundled with the
JDK we're using. That's what `@rules_scala_annex//native:defs.bzl%java_std_jars` does.

For more information on the Java Platform Module System and how it replaces Java 8's `rt.jar`, see:
https://en.wikipedia.org/wiki/Java_Platform_Module_System""",
        "native_scalac": "The native image for scalac.",
        "pgo": "Whether to output Profile-Guided Optimization files.",
    },
)
