"""
This extension contains copies of the `scala_binary`, `scala_library`, and `scala_test` rules from
`rules/scala.bzl` that provide formatting capabilities via Scalafmt. They're identical to the
afformentioned rules, but have two additional attributes:
- `config`
- `format`

Additionally, for every target created from one of the rules in this extension
(e.g. `//foo/bar:bizz`), you'll find two additional targets:
- `//foo/bar:bizz.format`
- `//foo/bar:bizz.format-test`

The former runs Scalafmt on the sources of the target, while the latter tests that those sources are
formatted.
"""

load(
    "@rules_scala_annex//rules:scala.bzl",
    "make_scala_binary",
    "make_scala_library",
    "make_scala_test",
)
load(
    "@rules_scala_annex//rules/scalafmt:ext.bzl",
    "ext_with_non_default_format",
)

scala_binary = make_scala_binary(ext_with_non_default_format)

scala_library = make_scala_library(ext_with_non_default_format)

scala_test = make_scala_test(ext_with_non_default_format)
