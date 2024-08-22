load(
    "@rules_scala_annex//rules:providers.bzl",
    _ScalaConfiguration = "ScalaConfiguration",
    _ScalaRulePhase = "ScalaRulePhase",
)

def run_phases(ctx, phases):
    toolchain = ctx.toolchains["//rules/scala:toolchain_type"]
    phase_providers = [toolchain.scala_rule_phases] + [
        phase_provider[_ScalaRulePhase]
        for phase_provider in ctx.attr.plugins + ctx.attr._phase_providers
        if _ScalaRulePhase in phase_provider
    ]

    if phase_providers != []:
        phases = adjust_phases(
            phases,
            [phase for phase_provider in phase_providers for phase in phase_provider.phases],
        )

    result_dict = {
        "init": struct(
            scala_configuration = toolchain.scala_configuration,
        ),
        "out": struct(
            output_groups = {},
            providers = [],
        ),
    }

    result = struct(**result_dict)

    for (name, function) in phases:
        addition = function(ctx, result)

        if addition != None:
            result_dict[name] = addition
            result = struct(**result_dict)

    return result

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
