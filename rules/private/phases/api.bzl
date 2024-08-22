load(
    "@rules_scala_annex//rules:providers.bzl",
    _ScalaConfiguration = "ScalaConfiguration",
    _ScalaRulePhase = "ScalaRulePhase",
)

def run_phases(ctx, phases):
    phase_providers = [
        p[_ScalaRulePhase]
        for p in [ctx.attr.scala] + ctx.attr.plugins + ctx.attr._phase_providers
        if _ScalaRulePhase in p
    ]

    if phase_providers != []:
        phases = adjust_phases(phases, [p for pp in phase_providers for p in pp.phases])

    gd = {
        "init": struct(
            scala_configuration = ctx.attr.scala[_ScalaConfiguration],
        ),
        "out": struct(
            output_groups = {},
            providers = [],
        ),
    }
    g = struct(**gd)
    for (name, function) in phases:
        p = function(ctx, g)
        if p != None:
            gd[name] = p
            g = struct(**gd)

    return g

def adjust_phases(phases, adjustments):
    if len(adjustments) == 0:
        return phases

    result = phases[:]

    for (relation, peer_name, name, function) in adjustments:
        for i, (needle, _) in enumerate(result):
            if needle == peer_name and relation in ["-", "before"]:
                result.insert(i, (name, function))

    # We iterate through the additions in reverse order so they're added in the same order as
    # they're defined
    for (relation, peer_name, name, function) in adjustments[::-1]:
        for i, (needle, _) in enumerate(result):
            if needle == peer_name and relation in ["+", "after"]:
                result.insert(i + 1, (name, function))

    return result
