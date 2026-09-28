# During partial syncing, the old Bazel IntelliJ plugin determines if a target is a Scala target by checking if it provides
# `ScalaInfo`, which it `load`s from `@rules_scala//scala:providers.bzl`, or if the rule name begins with `scala_`.
# https://github.com/bazelbuild/intellij/blob/87d40e651676a170fcb8dad5bcf2b1e3ffb376da/aspect/scala_info.bzl#L1-L19
#
# This is a dummy provider that will never be provided by any rule, but which forces the plugin to check the rule name
# instead of the provider. Without this provider being defined, partial syncing will fail because
# `@rules_scala//scala:providers.bzl%ScalaInfo` won't exist.
ScalaInfo = provider()
