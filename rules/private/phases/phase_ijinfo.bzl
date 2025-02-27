load(
    "//rules/scala:private/import.bzl",
    _create_intellij_info = "create_intellij_info",
)

#
# PHASE: ijinfo
#
# Creates IntelliJ info
#

def phase_ijinfo(ctx, g):
    g.out.providers.append(_create_intellij_info(ctx.label, ctx.attr.deps, g.javainfo.java_info))
