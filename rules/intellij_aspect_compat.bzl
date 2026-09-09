"""Compatibility helpers for JetBrains' IntelliJ Bazel aspect."""

load("@rules_java//java/common:java_common.bzl", "java_common")
load("@rules_java//java/common:java_info.bzl", "JavaInfo")

_SCALA_TOOLCHAIN_TYPE = "//rules/scala:toolchain_type"

def _intellij_scalac_classpath_impl(ctx):
    scala_configuration = ctx.toolchains[_SCALA_TOOLCHAIN_TYPE].scala_configuration
    compiler_classpath = java_common.merge([
        target[JavaInfo]
        for target in scala_configuration.compiler_classpath
    ])
    return [
        DefaultInfo(
            runfiles = ctx.runfiles(
                files = compiler_classpath.transitive_runtime_jars.to_list(),
            ),
        ),
    ]

intellij_scalac_classpath = rule(
    implementation = _intellij_scalac_classpath_impl,
    toolchains = [_SCALA_TOOLCHAIN_TYPE],
)

# By default we point the IntelliJ aspect at this target, which should handle transitions between
# Scala toolchains without issue. Both built-in and custom Scala toolchains should work.
_intellij_scalac_classpath = Label("@rules_scala_annex//rules/scala:intellij_scalac_classpath")

intellij_aspect_compat_attributes = {
    # The IntelliJ aspect uses this attribute as an indicator the target has a Scala toolchain
    "_scala_toolchain": attr.label(default = _intellij_scalac_classpath),

    # The IntelliJ aspect uses this attribute as an indicator the target has a scalac classpath.
    # It then searches through the target's runfiles to get the correct classpath.
    "_scalac": attr.label(default = _intellij_scalac_classpath),
}
