load("@rules_scala_annex//rules:scala.bzl", "scala_binary", "scala_library", "scala_test")

scala_library(
    name = "bad_compile",
    srcs = ["TestMacro.scala"],
    scala_version = "2.13",
    tags = ["manual"],
    deps = [
        ":macro_bad",
        "@annex_test//:org_scala_lang_scala_reflect",
    ],
)

scala_library(
    name = "macro",
    srcs = ["Macro.scala"],
    macro = True,
    scala_version = "2.13",
    deps = [
        ":util",
        "@annex_test//:org_scala_lang_scala_reflect",
    ],
)

scala_library(
    name = "macro_only",
    srcs = ["MacroOnly.scala"],
    macro = True,
    scala_version = "2.13",
    deps = [
        ":macro",
        "@annex_test//:org_scala_lang_scala_reflect",
    ],
)

scala_library(
    name = "macro_bad",
    srcs = ["Macro.scala"],
    scala_version = "2.13",
    deps = [
        ":util",
        "@annex_test//:org_scala_lang_scala_reflect",
    ],
)

scala_library(
    name = "util",
    srcs = ["MacroUtil.scala"],
    scala_version = "2.13",
    deps = [
        "@annex_test//:org_scala_lang_scala_reflect",
    ],
)

scala_binary(
    name = "test_macro",
    srcs = ["TestMacro.scala"],
    scala_version = "2.13",
    deps = [":macro"],
)

scala_binary(
    name = "test_macro_only",
    srcs = ["TestMacroOnly.scala"],
    deps_used_whitelist = [
        ":macro_only",
    ],
    scala_version = "2.13",
    deps = [
        ":macro",
        ":macro_only",
    ],
)

# The consumer can load macro implementations and their transitive dependencies
# even when the dependency is not marked as a macro provider.
scala_library(
    name = "full_compile_classpath_library",
    srcs = ["TestMacro.scala"],
    full_compile_classpath = True,
    scala_version = "2.13",
    deps = [":macro_bad"],
)

scala_binary(
    name = "full_compile_classpath_binary",
    srcs = ["TestMacro.scala"],
    full_compile_classpath = True,
    scala_version = "2.13",
    deps = [":macro_bad"],
)

scala_test(
    name = "full_compile_classpath_test",
    srcs = ["MacroTest.scala"],
    full_compile_classpath = True,
    scala_version = "2.13",
    deps = [
        ":macro_bad",
        "@annex_test//:junit_junit",
    ],
)

# Expanding a provider's own compile classpath must not mark it as a macro
scala_library(
    name = "macro_full_compile_classpath",
    srcs = ["Macro.scala"],
    full_compile_classpath = True,
    scala_version = "2.13",
    deps = [
        ":util",
        "@annex_test//:org_scala_lang_scala_reflect",
    ],
)

scala_library(
    name = "bad_compile_full_compile_classpath_provider",
    srcs = ["TestMacro.scala"],
    full_compile_classpath = False,
    scala_version = "2.13",
    tags = ["manual"],
    deps = [
        ":macro_full_compile_classpath",
        "@annex_test//:org_scala_lang_scala_reflect",
    ],
)
