"""Default bootstrap toolchain macro for users of annex.

This provides a safe way to customize the bootstrap Scala toolchain without introducing classpath
conflicts with annex's internal targets.
"""

load("//rules:register_toolchain.bzl", "register_bootstrap_toolchain")
load("//rules/scala:classpaths.bzl", "compiler_classpath_3", "runtime_classpath_3")
load("//rules/scala:scalac_options.bzl", "shared_global_scalacopts")
load("//rules/scala:versions.bzl", "scala_3_version")

def default_bootstrap_toolchain(
        name,
        jvm_flags = [],
        global_scalacopts = shared_global_scalacopts,
        global_plugins = [],
        prefix = "bootstrap",
        use_ijar = True,
        semanticdb_bundle = True,
        visibility = ["//visibility:public"]):
    """Creates a bootstrap Scala 3 toolchain with annex's default classpath.

    Use this instead of register_bootstrap_toolchain() when you need to customize the bootstrap
    toolchain, e.g., to add JVM options while ensuring compatibility with annex's internal targets.

    The compiler_classpath, runtime_classpath, and version are locked to annex's internal values to
    prevent classpath conflicts between the toolchain's Scala library and annex's transitive
    dependencies.

    Args:
        name: The name of the toolchain.
        jvm_flags: JVM options to pass when invoking Scala-related actions.
        global_scalacopts: Scalac options that will always be enabled.
            Defaults to ["-deprecation", "-Wconf:any:error"].
        global_plugins: Scalac plugins that will always be enabled.
        prefix: Prefix for scala_version disambiguation. Defaults to "bootstrap".
        use_ijar: Whether to use ijar for this compiler. Defaults to True.
        semanticdb_bundle: Whether to bundle SemanticDB files. Defaults to True.
        visibility: Visibility of the toolchain. Defaults to public.
    """
    register_bootstrap_toolchain(
        name = name,
        compiler_classpath = compiler_classpath_3,
        runtime_classpath = runtime_classpath_3,
        jvm_flags = jvm_flags,
        global_scalacopts = global_scalacopts,
        global_plugins = global_plugins,
        prefix = prefix,
        use_ijar = use_ijar,
        semanticdb_bundle = semanticdb_bundle,
        version = scala_3_version,
        visibility = visibility,
    )
