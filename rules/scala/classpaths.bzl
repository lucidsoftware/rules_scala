"""Classpaths used by annex's Scala toolchains.

These definitions use @annex Maven dependencies and are shared by both bootstrap and Zinc
toolchains defined in //src/main/scala:BUILD.

Label() is used to ensure these resolve relative to the rules_scala_annex module, not the consuming
repo's module. This is critical for the default_bootstrap_toolchain() macro to work correctly
across module boundaries.

Users of annex should use default_bootstrap_toolchain() from
//rules:default_bootstrap_toolchain.bzl rather than referencing these directly.
"""

compiler_classpath_2_13 = [
    Label("@annex//:org_scala_lang_scala_compiler"),
    Label("@annex//:org_scala_lang_scala_library"),
    Label("@annex//:org_scala_lang_scala_reflect"),
]

runtime_classpath_2_13 = [
    Label("@annex//:org_scala_lang_scala_library"),
]

compiler_classpath_3 = [
    Label("@annex//:org_scala_lang_scala3_compiler_3"),
    Label("@annex//:org_scala_lang_scala3_library_3"),
]

runtime_classpath_3 = [
    Label("@annex//:org_scala_lang_scala3_library_3"),
    Label("@annex//:org_scala_lang_scala3_interfaces"),
    Label("@annex//:org_scala_lang_tasty_core_3"),
]
