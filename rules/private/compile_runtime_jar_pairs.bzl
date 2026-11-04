load("@rules_java//java/common:java_info.bzl", "JavaInfo")

CompileRuntimeJarPair = provider(
    doc = "A compile JAR-runtime JAR pair for a given target.",
    fields = {
        "compile_jar": "The compile JAR.",
        "runtime_jar": "The runtime JAR.",
    },
)

CompileRuntimeJarPairsInfo = provider(
    doc = "Stores the compile JAR-runtime JAR pairs for a given target and its transitive dependencies.",
    fields = {
        "pairs": "A depset of `CompileRuntimeJarPair`s.",
    },
)

def _compile_runtime_jar_pairs_aspect_impl(target, ctx):
    return [
        CompileRuntimeJarPairsInfo(
            pairs = depset(
                direct = [
                    CompileRuntimeJarPair(
                        compile_jar = output.compile_jar,
                        runtime_jar = output.class_jar,
                    )
                    for output in target[JavaInfo].java_outputs
                ] if JavaInfo in target else [],
                transitive = [
                    dependency[CompileRuntimeJarPairsInfo].pairs
                    for dependency in getattr(ctx.rule.attr, "deps", [])
                    if CompileRuntimeJarPairsInfo in dependency
                ],
            ),
        ),
    ]

compile_runtime_jar_pairs_aspect = aspect(
    attr_aspects = ["deps"],
    doc = "Produces `CompileRuntimeJarPairsInfo`.",
    implementation = _compile_runtime_jar_pairs_aspect_impl,
    provides = [CompileRuntimeJarPairsInfo],
)
