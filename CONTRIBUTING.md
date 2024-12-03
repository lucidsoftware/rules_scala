# Contributing guidelines

Contributions should follow the [principles](README.md#principles) of `lucidsoftware/rules_scala`.

## Documentation

To generate the [Stardoc](https://github.com/bazelbuild/stardoc),

```
$ ./scripts/gen-docs.sh
```

## Formatting

[Buildifier](https://github.com/bazelbuild/buildtools/blob/main/buildifier) is used to format Skylark files,
and [Scalafmt](https://scalameta.org/scalafmt/) is used to format Scala files. To run them,

```
$ ./scripts/format.sh
```

## Maven deps

[rules_jvm_external](https://github.com/bazel-contrib/rules_jvm_external) is used to resolve Maven
dependencies. If you need to change dependencies, add your artifacts to the `annex*.install` calls
in [`MODULE.bazel`](MODULE.bazel) or [`tests/MODULE.bazel`](tests/MODULE.bazel).

To reference the dependency, use the `name` attribute of the `annex*.install` call as the
repository name and the versionless dependency as the target. E.g.
`@<maven_install_name>//:<versionless_dependency>`.

For example, if you'd like to add `org.scala-sbt:compiler-interface:1.2.1` as a dependency, simply
add it to the `artifacts` list of the `maven.install` call, and then refer to it with
`@annex//:org_scala_sbt_compiler_interface`.

```starlark
annex.install(
    name = "annex",
    artifacts = [
        ...,
        "org.scala-sbt:compiler-interface:1.2.1",
        ...,
    ],
    fetch_sources = True,
    lock_file = "//:annex_install.json",
    repositories = [
        "https://repo.maven.apache.org/maven2",
        "https://maven-central.storage-download.googleapis.com/maven2",
        "https://mirror.bazel.build/repo1.maven.org/maven2",
    ],
)
```

## Tests

Tests are bash shell scripts that may be run individually or together.
rules_scala_annex is tested on Ubuntu and OS X for the most recent thee minor versions of Bazel.

```
$ # runs all tests
$ ./scripts/test.sh
```

```
$ # runs all tests in tests/dependencies/
$ ./scripts/test.sh tests/dependencies/
```
