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
```python
load(
    "@rules_scala_annex//rules/register_toolchain.bzl",
    "register_bootstrap_toolchain",
    "register_zinc_toolchain",
)

compiler_classpath_2_13 = [
    "@scala_compiler_2_13//jar",
    "@scala_library_2_13//jar",
    "@scala_reflect_2_13//jar",
]

runtime_classpath_2_13 = ["@scala_library_2_13//jar"]

register_bootstrap_toolchain(
    name = "bootstrap_2_13",
    compiler_classpath = compiler_classpath_2_13,
    runtime_classpath = runtime_classpath_2_13,
    version = "2.13.14",
    visibility = ["//visibility:public"],
)

# compiler bridge needed to configure zinc compiler
scala_library(
    name = "compiler_bridge_2_13",
    srcs = ["@compiler_bridge_2_13//:src"],
    scala_toolchain_name = "bootstrap_2_13",
    visibility = ["//visibility:public"],
    deps = compiler_classpath_2_13 + [
        "@scala_annex_org_scala_sbt_compiler_interface//jar",
        "@scala_annex_org_scala_sbt_util_interface//jar",
    ],
)

# This augments the configuration to configure the zinc compiler
register_zinc_toolchain(
    name = "zinc_2_13",
    compiler_bridge = ":compiler_bridge_2_13",
    compiler_classpath = compiler_classpath_2_13,
    runtime_classpath = runtime_classpath_2_13,
    version = "2.13.14",
    visibility = ["//visibility:public"],
)
```

*/WORKSPACE*
```python
load("@rules_scala_annex//rules/scala:workspace.bzl", "scala_register_toolchains")

...

scala_register_toolchains(
    toolchains = ["//:bootstrap_2_13", "//:zinc_2_13"],
    default_scala_toolchain_name = "zinc_2_13",
)

...
```

Take note of the `scala_toolchain_name` attribute on `scala_library` and the other Scala rules. Each
toolchain that's registered via `scala_register_toolchains` is identified by its `name`. Individual
Scala targets can be made to use a particular toolchain by setting their `scala_toolchain_name`
attribute.

For example:

```python
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
