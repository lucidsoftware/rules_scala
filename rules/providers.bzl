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
