# Scalafmt

`lucidsoftware/rules_scala` contains copies of the default Scala rules with formatting capabilities,
powered by Scalafmt. See [the Stardoc on these rules](./stardoc/scala_with_scalafmt.md) for more
information.

[Toolchains](https://bazel.build/extending/toolchains) are used to set the Scalafmt
configuration file that's used by those targets that have formatting enabled. The default toolchain
uses the [`.scalafmt.conf`](../.scalafmt.conf) file at the root of this repository—the same
configuration file that's used to format this repository's code. All you need to do to use the
formatting rules is register the default toolchain with Bazel in your `MODULE.bazel` file:

```starlark
register_toolchains("@rules_scala_annex//:annex_scalafmt")
```

That should be sufficient to get you started, but if you'd like to use your own `.scalafmt.conf`
file, you'll need to declare your own toolchain and register it with Bazel:

*/BUILD*

```starlark
load("@rules_scala_annex//rules/scalafmt:register_toolchain.bzl", "register_scalafmt_toolchain")

register_scalafmt_toolchain(
    name = "custom_scalafmt",
    config = ".scalafmt.conf",
)
```

*/MODULE.bazel*

```starlark
register_toolchains(":custom_scalafmt")
```

Then, you can either:
- Use it for every target by default by adding the
  `--@rules_scala_annex//rules/scalafmt:scalafmt-toolchain=custom_scalafmt` flag to your `.bazelrc`
  file
- Use it for a specific target by setting the `scalafmt_toolchain_name` attribute:
    ```starlark
    load("@rules_scala_annex//rules:scala_with_scalafmt.bzl", "scala_binary")

    scala_binary(
        ...,
        scalafmt_toolchain_name = "custom_scalafmt",
        ...,
    )
    ```

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

Note that like the Scala rules, `scala_format_test` too uses toolchains and accepts a
`scalafmt_toolchain_name` attribute.
