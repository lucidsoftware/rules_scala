## Specifying the Scala version to use

We use a [toolchain](https://bazel.build/extending/toolchains) to store compiler configuration,
which includes:
- Which compiler to use
- What compile-time and runtime dependencies to add
- What compiler plugins and options to use
- Which Zinc compiler bridge to use
- etc.

We provide multiple macros for defining Scala toolchains:
- `register_zinc_toolchain` in `@rules_scala_annex//rules/register_toolchain.bzl` - for Zinc-based
  compilation. This is the primary toolchain for user Scala targets.
- `default_bootstrap_toolchain` in `@rules_scala_annex//rules/default_bootstrap_toolchain.bzl` - a
  convenience macro for customizing the bootstrap toolchain while maintaining classpath
  compatibility with annex's internal targets. This is the recommended way to customize the
  bootstrap toolchain.
- `register_bootstrap_toolchain` in `@rules_scala_annex//rules/register_toolchain.bzl` - for
  bootstrap compilation. This is used internally by annex. The Zinc toolchain requires the
  bootstrap toolchain.

### Setting the default Scala version

Set the default Scala version via the module extension in your `MODULE.bazel`:

```starlark
scala = use_extension("@rules_scala_annex//:extensions.bzl", "scala")
scala.defaults(scala_version = "2.13")
use_repo(scala, "rules_scala_annex_config")
```

### Registering toolchains

Register both bootstrap and zinc toolchains in a BUILD file, then register them in `MODULE.bazel`:

*/BUILD.bazel*

```starlark
load(
    "@rules_scala_annex//rules:default_bootstrap_toolchain.bzl",
    "default_bootstrap_toolchain",
)
load(
    "@rules_scala_annex//rules/register_toolchain.bzl",
    "register_zinc_toolchain",
)

# You'll need to pull these in via `rules_jvm_external`. Note that `@maven` should be replaced with
# the name of your dependency tree, as specified in the `name` attribute of `maven.install` or
# `maven.artifact`.
compiler_classpath_2_13 = [
    "@maven//:org_scala_lang_scala_compiler",
    "@maven//:org_scala_lang_scala_library",
    "@maven//:org_scala_lang_scala_reflect",
]

runtime_classpath_2_13 = ["@maven//:org_scala_lang_scala_library"]

# `default_bootstrap_toolchain` registers the bootstrap toolchain annex uses internally. It locks
# the compiler_classpath, runtime_classpath, and version to annex's Scala 3 values to avoid
# classpath conflicts, so it doesn't take those arguments. See "Customizing the bootstrap toolchain"
# below for the options it does accept.
default_bootstrap_toolchain(
    name = "bootstrap_3",
)

# This augments the configuration to configure the zinc compiler
register_zinc_toolchain(
    name = "zinc_2_13",
    # You'll need to pull this in via `rules_jvm_external`
    compiler_bridge = "@maven//:org_scala_sbt_compiler_bridge_2_13",
    compiler_classpath = compiler_classpath_2_13,
    runtime_classpath = runtime_classpath_2_13,
    version = "2.13.14",
    visibility = ["//visibility:public"],
)
```

*/MODULE.bazel*

```starlark
register_toolchains(
    "//:bootstrap_3",
    "//:zinc_2_13",
)
```

### Selecting the Scala version per target

Use the `scala_version` attribute on `scala_library` and other Scala rules to select which
toolchain to use. Toolchains are matched hierarchically, e.g., `2` or `"2.13"` matches a toolchain
registered with `version = "2.13.14"`.

```starlark
# Uses the default Scala version (from scala.defaults())
scala_library(
    name = "example_default",
    srcs = glob(["**/*.scala"]),
)

# Explicitly uses Scala 2.13
scala_library(
    name = "example_2_13",
    srcs = glob(["**/*.scala"]),
    scala_version = "2.13",
)

# Explicitly uses Scala 3.3.5
scala_library(
    name = "example_bootstrap",
    srcs = glob(["**/*.scala"]),
    scala_version = "3.3.5",
)
```

### Prefix-based disambiguation

When you have multiple toolchains for the same Scala version, e.g., one default and one with
special compiler plugins, use the `prefix` parameter on `register_zinc_toolchain` to
distinguish them:

```starlark
register_zinc_toolchain(
    name = "zinc_2_13_semanticdb",
    prefix = "semanticdb",
    version = "2.13.14",
    ...
)
```

Then select it with `scala_version = "semanticdb_2.13"`.

### Customizing the bootstrap toolchain

The bootstrap toolchain compiles annex's internal Scala targets (workers, test runners, etc.) using
`scalac` directly rather than the compiler bridge. Because annex's internal targets depend on Maven
artifacts from annex's own `@annex` repository, the bootstrap toolchain must use the same
dependencies. Using a different set of dependencies (e.g., from your repo's own `@maven`) will
cause classpath conflicts where the Scala standard library appears twice.

Use `default_bootstrap_toolchain()` to safely customize the bootstrap toolchain:

```starlark
load("@rules_scala_annex//rules:default_bootstrap_toolchain.bzl", "default_bootstrap_toolchain")

default_bootstrap_toolchain(
    name = "my_bootstrap_3",
    jvm_flags = ["--sun-misc-unsafe-memory-access=allow"],
)
```

This macro locks the `compiler_classpath`, `runtime_classpath`, and `version` to annex's internal
values while allowing you to customize:
- `jvm_flags`: JVM options for the compiler process (e.g., for newer JDK compatibility).
- `global_scalacopts`: Scalac options applied to all bootstrap compilations.
  Defaults to `["-deprecation", "-Wconf:any:error"]`.
- `global_plugins`: Scalac plugins to enable globally.
- `prefix`: Prefix for `scala_version` disambiguation. Defaults to `"bootstrap"`.
- `use_ijar`: Whether to use ijar. Defaults to `True`.
- `semanticdb_bundle`: Whether to bundle SemanticDB files. Defaults to `True`.

Then register the toolchain in `MODULE.bazel`:

```starlark
register_toolchains(
    "//:my_bootstrap_3",
    "//:zinc_2_13",
    "//:zinc_3",
)
```

### Scalafmt toolchains and `scala_version`

Scalafmt toolchains are also matched based on the `scala_version` setting. Each scalafmt toolchain
lists the Scala versions it is compatible with via the `scala_versions` parameter. Please use the
same Scala versions that you used for your Zinc toolchains.

Prefixed variants of toolchains can be included in the `scala_versions` list.

```starlark
load("@rules_scala_annex//rules/scalafmt:register_toolchain.bzl", "register_scalafmt_toolchain")

register_scalafmt_toolchain(
    name = "my_scalafmt",
    config = ".scalafmt.conf",
    scala_versions = [
        "2.13.14",
        "semanticdb_2.13.14",
    ],
)

register_scalafmt_toolchain(
    name = "my_scalafmt_3",
    config = ".scalafmt-scala3.conf",
    scala_versions = [
        "3.3.5",
        "bootstrap_3.3.5",
        "semanticdb_3.3.5",
    ],
)
```

See [scalafmt.md](../scalafmt.md) for more information.
