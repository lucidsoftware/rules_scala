# Scalafmt

`lucidsoftware/rules_scala` contains copies of the default Scala rules with formatting capabilities,
powered by Scalafmt. See [the Stardoc on these rules](./stardoc/scala_with_scalafmt.md) for more
information.

[Toolchains](https://bazel.build/extending/toolchains) are used to set the Scalafmt
configuration file that's used by those targets that have formatting enabled. Scalafmt toolchains
are matched to targets based on the `scala_version` setting, so you need to associate a Scalafmt
toolchain with each version of Scala you want formatting to occur for.

## Quick start

The default toolchain, `@rules_scala_annex//:annex_scalafmt`, uses the
[`.scalafmt.conf`](../.scalafmt.conf) that ships with Annex itself at the root of the
`rules_scala_annex` repository. You can register it in
your `MODULE.bazel` file:

```starlark
register_toolchains("@rules_scala_annex//:annex_scalafmt")
```

To format your code with your own Scalafmt configuration, define a [custom
toolchain](#custom-toolchains) instead.

## Custom toolchains

To use your own `.scalafmt.conf` file, declare your own toolchain with the `scala_versions` it
should apply to:

*/BUILD*

```starlark
load("@rules_scala_annex//rules/scalafmt:register_toolchain.bzl", "register_scalafmt_toolchain")

register_scalafmt_toolchain(
    name = "custom_scalafmt",
    config = ".scalafmt.conf",
    scala_versions = ["2.13.16"],
)
```

*/MODULE.bazel*

```starlark
register_toolchains(":custom_scalafmt")
```

### The `scala_versions` parameter

When registering a custom scalafmt toolchain, you will need to list Scala versions the toolchain is
compatible with using the `scala_versions` parameter. These versions should match the versions you
used for  your Zinc toolchains. For example, use `"3.3.5"`, not `"3"` or `"3.3"`.

Each version you list is matched hierarchically, so you only need the full version: listing
`"3.3.5"` covers targets whose `scala_version` is `"3"`, `"3.3"`, or `"3.3.5"`.

For prefixed toolchains (such as `semanticdb` or `bootstrap`), add the prefix to the version. For
example, `"semanticdb_3.3.5"`:

```starlark
register_scalafmt_toolchain(
    name = "custom_scalafmt",
    config = ".scalafmt.conf",
    scala_versions = [
        "2.13.16",
        "semanticdb_2.13.16",
    ],
)

register_scalafmt_toolchain(
    name = "custom_scalafmt_3",
    config = ".scalafmt-scala3.conf",
    scala_versions = [
        "3.3.5",
        "bootstrap_3.3.5",
        "semanticdb_3.3.5",
    ],
)
```

All `scala_version` values used in the build (including prefixed ones) must be covered by a
registered scalafmt toolchain.

### The `jvm_flags` parameter

If you need to pass JVM options to the JVM which runs Scalafmt, you can use the `jvm_flags`
parameter on the toolchain. For example:

```starlark
register_scalafmt_toolchain(
    name = "custom_scalafmt",
    config = ".scalafmt.conf",
    scala_versions = ["2.13.16"],
    jvm_flags = ["--sun-misc-unsafe-memory-access=allow"],
)
```

## Standalone formatting

If you'd like to format all of the Scala files in your repository via a single target, you can use
`scala_format_test`:

*/BUILD*

```starlark
load("@rules_scala_annex//rules:scalafmt.bzl", "scala_format_test")
scala_format_test(
    name = "format",
    srcs = glob(["**/*.scala"]),
)
```

Then:

```
# check format, with diffs and non-zero exit in case of differences
$ bazel test :format

# format files in-place
$ bazel run :format
```

Note that like the Scala rules, `scala_format_test` accepts a `scala_version` attribute to select
the correct scalafmt toolchain for non-default Scala versions:

```starlark
scala_format_test(
    name = "format-scala3",
    srcs = glob(["**/*.scala"]),
    scala_version = "3",
)
```
