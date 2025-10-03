load("//rules:providers.bzl", "LabeledJars")
load("//rules/jvm:private/label.bzl", "get_labeled_jars")

#
# PHASE: labeledjars
#
# Outputs the `LabeledJars` provider if `deps_checker_label` is set.
#

def phase_labeledjars(ctx, g):
    if ctx.attr.deps_checker_label != "":
        g.out.providers.append(get_labeled_jars(ctx.attr.deps_checker_label, g.javainfo.java_info, ctx.attr.deps))
