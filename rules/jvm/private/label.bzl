load("@rules_java//java/common:java_info.bzl", "JavaInfo")
load(
    "@rules_scala_annex//rules:providers.bzl",
    _LabeledJars = "LabeledJars",
    _LabeledJarsData = "LabeledJarsData",
)

def get_labeled_jars(label, java_info, deps):
    deps_labeled_jars = [dep[_LabeledJars] for dep in deps if _LabeledJars in dep]
    return _LabeledJars(
        label = label,
        values = depset(
            [
                _LabeledJarsData(
                    label = label,
                    jars = depset(transitive = [java_info.compile_jars, java_info.full_compile_jars]),
                ),
            ],
            order = "preorder",
            transitive = [labeled_jars.values for labeled_jars in deps_labeled_jars],
        ),
    )

def labeled_jars_implementation(target, ctx):
    if JavaInfo not in target:
        return []

    if _LabeledJars in target:
        return []

    return [get_labeled_jars(ctx.label, target[JavaInfo], getattr(ctx.rule.attr, "deps", []))]
