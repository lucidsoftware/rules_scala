"""
Provides compatibility with vanilla rules_scala rules

The aim is to implement compatibility strictly with macros. `bazel query`
can be used to expand macro usage and provide a seamless transition to
newer rules.
"""

load(
    "//rules:providers.bzl",
    "ScalaConfiguration",
)
load(
    "//rules:scala.bzl",
    _scala_binary = "scala_binary",
    _scala_library = "scala_library",
    _scala_test = "scala_test",
)
load(
    "//rules/common:private/utils.bzl",
    _safe_name = "safe_name",
)

_extra_deps = ["//external:scala_annex/compat/rules_scala/extra_deps"]

_scalatest_deps = ["//external:scala_annex/compat/rules_scala/scalatest_dep"]

def scala_library(
        # bazel rule attributes
        name,
        tags = [],
        visibility = None,
        # rules_scala common attributes
        data = [],
        deps = [],
        javac_jvm_flags = [],
        javacopts = [],
        jvm_flags = [],
        plugins = [],
        print_compile_time = False,
        resource_jars = [],
        resource_strip_prefix = None,
        resources = [],
        runtime_deps = [],
        scala_toolchain_name = None,
        scalac_jvm_flags = [],
        scalacopts = [],
        srcs = [],
        # library only attributes
        main_class = None,
        exports = [],
        # compat layer internals
        _use_ijar = True):
    if plugins != []:
        print("%s: plugins unsupported" % name)
    if data != []:
        print("%s: data unsupported" % name)
    if resources != []:
        print("%s: resources unsupported" % name)
    if resource_strip_prefix != None:
        print("%s: resource_strip_prefix unsupported" % name)
    if resource_jars != []:
        print("%s: resource_jars unsupported" % name)
    if javacopts != []:
        print("%s: javacopts unsupported" % name)
    if jvm_flags != []:
        print("%s: jvm_flags unsupported" % name)
    if scalac_jvm_flags != []:
        print("%s: scalac_jvm_flags unsupported" % name)
    if javac_jvm_flags != []:
        print("%s: javac_jvm_flags unsupported" % name)
    if print_compile_time != False:
        print("%s: print_compile_time unsupported" % name)
    if main_class != None:
        print("%s: main_class unsupported" % name)

    deps = deps if deps else []
    _scala_library(
        name = name,
        srcs = srcs,
        deps_used_whitelist = _extra_deps,
        exports = exports,
        macro = not _use_ijar,
        runtime_deps = runtime_deps,
        scala_toolchain_name = scala_toolchain_name,
        scalacopts = scalacopts,
        tags = tags,
        visibility = visibility,
        deps = deps + _extra_deps,
    )

def scala_macro_library(
        # bazel rule attributes
        name,
        tags = [],
        visibility = None,
        # rules_scala common attributes
        data = [],
        deps = [],
        javac_jvm_flags = [],
        javacopts = [],
        jvm_flags = [],
        plugins = [],
        print_compile_time = False,
        resource_jars = [],
        resource_strip_prefix = None,
        resources = [],
        runtime_deps = [],
        scala_toolchain_name = None,
        scalac_jvm_flags = [],
        scalacopts = [],
        srcs = [],
        # library only attributes
        main_class = None,
        exports = []):
    return scala_library(
        name,
        tags,
        visibility,
        data,
        deps,
        javac_jvm_flags,
        javacopts,
        jvm_flags,
        plugins,
        print_compile_time,
        resource_jars,
        resource_strip_prefix,
        resources,
        runtime_deps,
        scala_toolchain_name,
        scalac_jvm_flags,
        scalacopts,
        srcs,
        main_class,
        exports,
        False,  # _use_ijar
    )

def scala_binary(
        # bazel rule attributes
        name,
        tags = [],
        visibility = None,
        # rules_scala common attributes
        data = [],
        deps = [],
        javac_jvm_flags = [],
        javacopts = [],
        jvm_flags = [],
        plugins = [],
        print_compile_time = False,
        resource_jars = [],
        resource_strip_prefix = None,
        resources = [],
        runtime_deps = [],
        scala_toolchain_name = None,
        scalac_jvm_flags = [],
        scalacopts = [],
        srcs = [],
        # binary only attributes
        main_class = None,
        classpath_resources = [],
        # compat layer internals
        _use_ijar = True):
    if plugins != []:
        print("%s: plugins unsupported" % name)
    if data != []:
        print("%s: data unsupported" % name)
    if resources != []:
        print("%s: resources unsupported" % name)
    if resource_strip_prefix != None:
        print("%s: resource_strip_prefix unsupported" % name)
    if resource_jars != []:
        print("%s: resource_jars unsupported" % name)
    if scalacopts != []:
        print("%s: scalacopts unsupported" % name)
    if javacopts != []:
        print("%s: javacopts unsupported" % name)
    if jvm_flags != []:
        print("%s: jvm_flags unsupported" % name)
    if scalac_jvm_flags != []:
        print("%s: scalac_jvm_flags unsupported" % name)
    if javac_jvm_flags != []:
        print("%s: javac_jvm_flags unsupported" % name)
    if print_compile_time != False:
        print("%s: print_compile_time unsupported" % name)
    if classpath_resources != []:
        print("%s: classpath_resources unsupported" % name)

    deps = deps if deps else []
    _scala_binary(
        name = name,
        srcs = srcs,
        deps_used_whitelist = _extra_deps,
        main_class = main_class,
        runtime_deps = runtime_deps,
        scala_toolchain_name = scala_toolchain_name,
        tags = tags,
        deps = deps + _extra_deps,
    )

def scala_test(
        # bazel rule attributes
        name,
        tags = [],
        visibility = None,
        # rules_scala common attributes
        data = [],
        deps = [],
        javac_jvm_flags = [],
        javacopts = [],
        jvm_flags = [],
        plugins = [],
        print_compile_time = False,
        resource_jars = [],
        resource_strip_prefix = None,
        resources = [],
        runtime_deps = [],
        scala_toolchain_name = None,
        scalac_jvm_flags = [],
        scalacopts = [],
        srcs = [],
        # test only attributes
        suites = [],
        colors = None,
        full_stacktraces = None,
        # compat layer internals
        _use_ijar = True,
        **kwargs):
    if plugins != []:
        print("%s: plugins unsupported" % name)
    if data != []:
        print("%s: data unsupported" % name)
    if resources != []:
        print("%s: resources unsupported" % name)
    if resource_strip_prefix != None:
        print("%s: resource_strip_prefix unsupported" % name)
    if resource_jars != []:
        print("%s: resource_jars unsupported" % name)
    if scalacopts != []:
        print("%s: scalacopts unsupported" % name)
    if javacopts != []:
        print("%s: javacopts unsupported" % name)
    if jvm_flags != []:
        print("%s: jvm_flags unsupported" % name)
    if scalac_jvm_flags != []:
        print("%s: scalac_jvm_flags unsupported" % name)
    if javac_jvm_flags != []:
        print("%s: javac_jvm_flags unsupported" % name)
    if print_compile_time != False:
        print("%s: print_compile_time unsupported" % name)
    if suites != []:
        print("%s: suites unsupported" % name)
    if colors != None:
        print("%s: colors unsupported" % name)
    if full_stacktraces != None:
        print("%s: full_stacktraces unsupported" % name)

    deps = deps if deps else []
    _scala_test(
        name = name,
        srcs = srcs,
        deps_used_whitelist = _extra_deps,
        frameworks = ["org.scalatest.tools.Framework"],
        runtime_deps = runtime_deps,
        scala_toolchain_name = scala_toolchain_name,
        tags = tags,
        deps = deps + _scalatest_deps + _extra_deps,
    )

def scala_test_suite(
        name,
        srcs = [],
        colors = True,
        data = [],
        deps = [],
        full_stacktraces = True,
        jvm_flags = [],
        resources = [],
        runtime_deps = [],
        scala_toolchain_name = None,
        scalacopts = [],
        size = None,
        visibility = None):
    tests = []
    for src in srcs:
        test_name = "%s_test_suite_%s" % (name, _safe_name(src))
        scala_test(
            name = test_name,
            srcs = [src],
            colors = colors,
            full_stacktraces = full_stacktraces,
            jvm_flags = jvm_flags,
            resources = resources,
            runtime_deps = runtime_deps,
            scala_toolchain_name = scala_toolchain_name,
            scalacopts = scalacopts,
            size = size,
            visibility = visibility,
            deps = deps,
        )
        tests.append(test_name)

    native.test_suite(
        name = name,
        tests = tests,
        visibility = visibility,
    )
