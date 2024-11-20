# lucidsoftware/rules_scala

[![Build Status](https://github.com/lucidsoftware/rules_scala/workflows/CI/badge.svg)](https://github.com/lucidsoftware/rules_scala/actions)

Previously known as [higherkindness/rules_scala](https://github.com/higherkindness/rules_scala),
`lucidsoftware/rules_scala` evolved, in part, from the need for Bazel adoption support for large,
monorepo Scala projects. Bazel is wonderful because it makes use of parallelism and caching to
vastly improve build times. However, to see these benefits, a project must first be broken down into
tiny packages and make use of fine-grained dependencies. This is not always a realistic short-term
goal for large, monorepo Scala projects.

`lucidsoftware/rules_scala` allows for the optional use of Zinc incremental compilation to provide a
stepping stone for these projects as they migrate to Bazel. Although we've verified it to be correct
and determinisitc, we recommend leaving this disabled, as fine-grained and isolated targets are
more in-line with the [Bazel philosophy](https://bazel.build/basics/hermeticity).

`lucidsoftware/rules_scala` is written with maintainability and accessibility in mind. It aims to
facilitate the transition to Bazel, and to satisfy use cases throughout the Scala ecosystem.

## Principles

1. Support the breadth of the Scala ecosystem.
2. Follow Bazel best practices.
3. Be accessible and maintainable.
4. Have high-quality documentation.

If the right design principles are kept, implementing additional features should be simple and
straightforward.

## Features

* Simple core API modeled after Bazel's Java APIs
  * [scala_library](docs/stardoc/scala.md#scala_library)
  * [scala_binary](docs/stardoc/scala.md#scala_binary)
  * [scala_test](docs/stardoc/scala.md#scala_test)
  * [scala_import](docs/stardoc/scala.md#scala_import)
  * [scala_repl](docs/stardoc/scala.md#scala_repl)
* [Works with all sbt-compatible test frameworks](docs/scala.md#tests)
* [Advanced Dependency Detection](docs/scala.md#strict--unused-deps)
  * Errors on indirect and unused dependencies
  * Buildozer suggestions for dependency errors
* [Optional Worker strategy](docs/scala.md#workers)
* [Optional Zinc-based stateful incremental compilation](docs/stateful.md#stateful-compilation)
* [Scalafmt](docs/scalafmt.md#scalafmt) integration
* Protobuf support with ScalaPB
  * [scala_proto_library](docs/stardoc/scala_proto.md#scala_proto_library)
  * [scala_proto_toolchain](docs/stardoc/scala_proto.md#scala_proto_toolchain)
* Seamless integration with the [Bazel IntelliJ plugin](https://github.com/bazelbuild/intellij)
* [Customizable rules](docs/newdocs/phases.md#customizing-the-core-rules)
* [Multiple Scala versions in one build](docs/newdocs/scala_versions.md#specifying-the-scala-version-to-use), including Scala 3 (Dotty).
* [Optimal handling of macros and ijars](docs/newdocs/macros.md#macros-and-ijars)
* [Pass flags to Zinc compiler](docs/newdocs/zinc_flags.md)
* Modern implementation using Bazel's most idiomatic APIs

## Usage

WORKSPACE

```python
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# rules_java
http_archive(
    name = "rules_java",
    sha256 = "a9690bc00c538246880d5c83c233e4deb83fe885f54c21bb445eb8116a180b83",
    url = "https://github.com/bazelbuild/rules_java/releases/download/7.12.2/rules_java-7.12.2.tar.gz",
)

# Load rules_scala_annex
rules_scala_annex_version = "lucid_2024-11-18"

http_archive(
    name = "rules_scala_annex",
    integrity = "sha256-WjZvojiclkiyVxQ1NqkH1lDeGaDLyzQOGiDsCfhVAec=",
    strip_prefix = "rules_scala-{}".format(rules_scala_annex_version),
    type = "zip",
    url = "https://github.com/lucidsoftware/rules_scala/archive/{}.zip".format(rules_scala_annex_version),
)

rules_jvm_external_version = "6.1"

http_archive(
    name = "rules_jvm_external",
    sha256 = "42a6d48eb2c08089961c715a813304f30dc434df48e371ebdd868fc3636f0e82",
    strip_prefix = "rules_jvm_external-{}".format(rules_jvm_external_version),
    type = "zip",
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/{}.zip".format(rules_jvm_external_version),
)

load(
    "@rules_scala_annex//rules/scala:workspace.bzl",
    "scala_register_toolchains",
    "scala_repositories",
)

load(
    "@rules_scala_annex//rules/scala_proto:workspace.bzl",
    "scala_proto_register_toolchains",
    "scala_proto_repositories",
)

load(
    "@rules_scala_annex//rules/scalafmt:workspace.bzl",
    "scalafmt_default_config",
    "scalafmt_repositories",
)

scala_repositories()

load("@annex//:defs.bzl", annex_pinned_maven_install = "pinned_maven_install")

annex_pinned_maven_install()

scala_register_toolchains(default_scala_toolchain_name = "annex_zinc_3")
scalafmt_repositories()

load("@annex_scalafmt//:defs.bzl", annex_scalafmt_pinned_maven_install = "pinned_maven_install")

annex_scalafmt_pinned_maven_install()
scalafmt_default_config()
scala_proto_repositories()

load("@annex_proto//:defs.bzl", annex_proto_pinned_maven_install = "pinned_maven_install")

annex_proto_pinned_maven_install()
scala_proto_register_toolchains()
```

BUILD

```python
load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(
    name = "example",
    srcs = glob(["*.scala"])
)
```

## Further Documentation

See [contributing guidlines](CONTRIBUTING.md) for help on contributing to this project.

For all rules and attributes, see the [Stardoc](docs/stardoc).
