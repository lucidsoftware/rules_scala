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

`lucidsoftware/rules_scala` isn't on the [Bazel Central Registry](https://registry.bazel.build/), so
you'll need to pull it in via `archive_override`. Be sure to replace `<COMMIT>` with the
latest commit on `lucid-master` and `<INTEGRITY>` with the hash suggested by Bazel after the
dependency is first loaded.

MODULE.bazel

```starlark
bazel_dep(name = "rules_scala_annex")

rules_scala_annex_version = "<COMMIT>"

archive_override(
    module_name = "rules_scala_annex",
    integrity = "<INTEGRITY>",
    strip_prefix = "rules_scala-{}".format(rules_scala_annex_version),
    urls = ["https://github.com/lucidsoftware/rules_scala/archive/refs/heads/{}.zip".format(rules_scala_annex_version)],
)
```

BUILD

```starlark
load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(
    name = "example",
    srcs = glob(["*.scala"])
)
```

## Further Documentation

See [contributing guidelines](CONTRIBUTING.md) for help on contributing to this project.

For all rules and attributes, see the [Stardoc](docs/stardoc).
