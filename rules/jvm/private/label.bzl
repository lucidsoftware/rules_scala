load("@rules_java//java/common:java_info.bzl", "JavaInfo")
load(
    "@rules_scala_annex//rules:providers.bzl",
    _LabeledJars = "LabeledJars",
    _LabeledJarsData = "LabeledJarsData",
)

def labeled_jars_implementation(target, ctx):
    if JavaInfo not in target:
        return []

    deps_labeled_jars = [dep[_LabeledJars] for dep in getattr(ctx.rule.attr, "deps", []) if _LabeledJars in dep]
    java_info = target[JavaInfo]
    return [
        _LabeledJars(
            values = depset(
                [
                    _LabeledJarsData(
                        label = ctx.label,
                        jars = depset(transitive = [java_info.compile_jars, java_info.full_compile_jars]),
                    ),
                ],
                order = "preorder",
                transitive = [labeled_jars.values for labeled_jars in deps_labeled_jars],
            ),
        ),
    ]
