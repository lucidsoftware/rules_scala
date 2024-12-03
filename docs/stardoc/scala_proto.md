<!-- Generated with Stardoc: http://skydoc.bazel.build -->



<a id="scala_proto_library"></a>

## scala_proto_library

<pre>
load("@rules_scala_annex//rules:scala_proto.bzl", "scala_proto_library")

scala_proto_library(<a href="#scala_proto_library-name">name</a>, <a href="#scala_proto_library-deps">deps</a>, <a href="#scala_proto_library-grpc">grpc</a>)
</pre>

Generates Scala code from proto sources. The output is a `.srcjar` that can be passed into other rules for compilation.

See example use in [/tests/proto/BUILD](/tests/proto/BUILD)

**ATTRIBUTES**


| Name  | Description | Type | Mandatory | Default |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| <a id="scala_proto_library-name"></a>name |  A unique name for this target.   | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required |  |
| <a id="scala_proto_library-deps"></a>deps |  The proto_library targets you wish to generate Scala from   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_proto_library-grpc"></a>grpc |  -   | Boolean | optional |  `False`  |


<a id="scala_proto_toolchain"></a>

## scala_proto_toolchain

<pre>
load("@rules_scala_annex//rules:scala_proto.bzl", "scala_proto_toolchain")

scala_proto_toolchain(<a href="#scala_proto_toolchain-name">name</a>, <a href="#scala_proto_toolchain-compiler">compiler</a>, <a href="#scala_proto_toolchain-compiler_supports_workers">compiler_supports_workers</a>)
</pre>

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

**ATTRIBUTES**


| Name  | Description | Type | Mandatory | Default |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| <a id="scala_proto_toolchain-name"></a>name |  A unique name for this target.   | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required |  |
| <a id="scala_proto_toolchain-compiler"></a>compiler |  The compiler to use to generate Scala form proto sources   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `None`  |
| <a id="scala_proto_toolchain-compiler_supports_workers"></a>compiler_supports_workers |  -   | Boolean | optional |  `False`  |


