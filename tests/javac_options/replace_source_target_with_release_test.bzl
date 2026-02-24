"""Unit tests for replace_source_target_with_release."""

load("@bazel_skylib//lib:unittest.bzl", "asserts", "unittest")
load(
    "@rules_scala_annex//rules/common:private/javac_options.bzl",
    "replace_source_target_with_release",
)

def _transforms_matching_source_target_test_impl(ctx):
    env = unittest.begin(ctx)

    # -source/-target with matching versions become --release.
    asserts.equals(
        env,
        ["--release", "21"],
        replace_source_target_with_release(["-source", "21", "-target", "21"]),
    )

    # Surrounding options are preserved (in order) after the injected --release.
    asserts.equals(
        env,
        ["--release", "25", "-g", "-encoding", "UTF-8"],
        replace_source_target_with_release(
            ["-g", "-source", "25", "-target", "25", "-encoding", "UTF-8"],
        ),
    )

    return unittest.end(env)

_transforms_matching_source_target_test = unittest.make(_transforms_matching_source_target_test_impl)

def _leaves_non_matching_opts_unchanged_test_impl(ctx):
    env = unittest.begin(ctx)

    # Mismatched -source/-target versions are left as-is.
    mismatched = ["-source", "25", "-target", "21"]
    asserts.equals(env, mismatched, replace_source_target_with_release(mismatched))

    # Only -source (no -target) is left as-is.
    only_source = ["-source", "21"]
    asserts.equals(env, only_source, replace_source_target_with_release(only_source))

    # Only -target (no -source) is left as-is.
    only_target = ["-target", "21"]
    asserts.equals(env, only_target, replace_source_target_with_release(only_target))

    # Options without -source/-target are untouched.
    unrelated = ["-g", "-deprecation"]
    asserts.equals(env, unrelated, replace_source_target_with_release(unrelated))

    # A dangling -source with no following value is untouched.
    dangling = ["-g", "-source"]
    asserts.equals(env, dangling, replace_source_target_with_release(dangling))

    # The empty list is a no-op.
    asserts.equals(env, [], replace_source_target_with_release([]))

    return unittest.end(env)

_leaves_non_matching_opts_unchanged_test = unittest.make(_leaves_non_matching_opts_unchanged_test_impl)

def _respects_release_and_bootclasspath_guards_test_impl(ctx):
    env = unittest.begin(ctx)

    # --release is mutually exclusive with -source/-target, so leave a pre-existing one alone.
    with_release = ["--release", "21", "-source", "21", "-target", "21"]
    asserts.equals(env, with_release, replace_source_target_with_release(with_release))

    # --release is mutually exclusive with -bootclasspath, so don't inject one alongside it.
    with_bootclasspath = ["-source", "21", "-target", "21", "-bootclasspath", "/some/path"]
    asserts.equals(env, with_bootclasspath, replace_source_target_with_release(with_bootclasspath))

    return unittest.end(env)

_respects_release_and_bootclasspath_guards_test = unittest.make(_respects_release_and_bootclasspath_guards_test_impl)

def replace_source_target_with_release_test_suite(name):
    unittest.suite(
        name,
        _transforms_matching_source_target_test,
        _leaves_non_matching_opts_unchanged_test,
        _respects_release_and_bootclasspath_guards_test,
    )
