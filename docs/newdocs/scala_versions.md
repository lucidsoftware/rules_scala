## Specifying the Scala version to use

We use a [toolchain](https://bazel.build/extending/toolchains) to store compiler configuration,
which includes:
- Which compiler to use
- What compile-time and runtime dependencies to add
- What compiler plugins and options to use
- Which Zinc compiler bridge to use
- etc.

We provide two macros for defining Scala toolchains: `register_bootstrap_toolchain` and
`register_zinc_toolchain`, both of which are in `@rules_scala_annex//rules/register_toolchain.bzl`.
The latter requires the former.

Once you've registered both types of toolchains, you'll need to tell Bazel about them and set the
default one (which we recommend is a Zinc toolchain so you can get things like
unused/undeclared dependency checking and test code coverage checking) via the
`scala_register_toolchains` repository rule. Something like this should work, assuming this
repository is mapped to `rules_scala_annex`:

*/BUILD.bazel*

```starlark
load(
    "@rules_scala_annex//rules/register_toolchain.bzl",
    "register_bootstrap_toolchain",
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

# You'll need to pull thus in via `rules_jvm_external`
runtime_classpath_2_13 = ["@maven//:org_scala_lang_scala_library",]

register_bootstrap_toolchain(
    name = "bootstrap_2_13",
    compiler_classpath = compiler_classpath_2_13,
    runtime_classpath = runtime_classpath_2_13,
    version = "2.13.14",
    visibility = ["//visibility:public"],
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
    "//:bootstrap_2_13",
    "//:zinc_2_13",
)
```

*/.bazelrc*

```
common --@rules_scala_annex//rules/scala:scala-toolchain=zinc_2_13
```

Take note of the `scala_toolchain_name` attribute on `scala_library` and the other Scala rules. Each
toolchain that's registered via `scala_register_toolchains` is identified by its `name`. Individual
Scala targets can be made to use a particular toolchain by setting their `scala_toolchain_name`
attribute.

For example:

```starlark
scala_library(
    name = "example_compiled_with_scalac",
    srcs = glob(["**/*.scala"])
    scala_toolchain_name = "bootstrap_2_13",
)

scala_library(
    name = "example_compiled_with_zinc",
    srcs = glob(["**/*.scala"])
    scala_toolchain_name = "zinc_2_13",
)

# This would use the default toolchain, which we configured via `scala_register_toolchains` above
scala_library(
    name = "example_compiled_with_default_scala",
    srcs = glob(["**/*.scala"])
)
```
