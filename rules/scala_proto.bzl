load("@bazel_skylib//lib:dicts.bzl", _dicts = "dicts")
load(
    "//rules/scala_proto:private/core.bzl",
    _scala_proto_library_implementation = "scala_proto_library_implementation",
    _scala_proto_library_private_attributes = "scala_proto_library_private_attributes",
)

scala_proto_library = rule(
    attrs = _dicts.add(
        _scala_proto_library_private_attributes,
        {
            "deps": attr.label_list(
                doc = "The proto_library targets you wish to generate Scala from",
            ),
            "grpc": attr.bool(),
            "_zipper": attr.label(cfg = "exec", default = "@bazel_tools//tools/zip:zipper", executable = True),
        },
    ),
    doc = """
Generates Scala code from proto sources. The output is a `.srcjar` that can be passed into other rules for compilation.

See example use in [/tests/proto/BUILD](/tests/proto/BUILD)
""",
    toolchains = [
        "@rules_scala_annex//rules/scala_proto:compiler_toolchain_type",
    ],
    outputs = {
        "srcjar": "%{name}.srcjar",
    },
    implementation = _scala_proto_library_implementation,
)

def _scala_proto_toolchain_implementation(ctx):
    return [platform_common.ToolchainInfo(
        compiler = ctx.attr.compiler,
        compiler_supports_workers = ctx.attr.compiler_supports_workers,
        protoc = ctx.executable.protoc,
    )]

scala_proto_toolchain = rule(
    attrs = {
        "compiler": attr.label(
            allow_files = True,
            cfg = "exec",
            doc = "The compiler to use to generate Scala from proto sources",
            executable = True,
            mandatory = True,
        ),
        "compiler_supports_workers": attr.bool(default = False),
        "protoc": attr.label(
            allow_single_file = True,
            cfg = "exec",
            default = Label("@protobuf//:protoc"),
            doc = "The protoc binary to use",
            executable = True,
        ),
    },
    doc = """
Specifies a toolchain of the `@rules_scala_annex//rules/scala_proto:compiler_toolchain_type` toolchain type.

This rule should be used with an accompanying `toolchain` that binds it and specifies constraints
(See the official documentation for more info on [Bazel Toolchains](https://docs.bazel.build/versions/master/toolchains.html))

For example:

```starlark
scala_proto_toolchain(
    name = "scalapb_toolchain_example",
    compiler = ":worker",
    compiler_supports_workers = True,
    visibility = ["//visibility:public"],
)

toolchain(
    name = "scalapb_toolchain_example_linux",
    toolchain = ":scalapb_toolchain_example",
    toolchain_type = "@rules_scala_annex//rules/scala_proto:compiler_toolchain_type",
    exec_compatible_with = [
        "@bazel_tools//platforms:linux",
        "@bazel_tools//platforms:x86_64",
    ],
    target_compatible_with = [
        "@bazel_tools//platforms:linux",
        "@bazel_tools//platforms:x86_64",
    ],
    visibility = ["//visibility:public"],
)
```
""",
    implementation = _scala_proto_toolchain_implementation,
)
