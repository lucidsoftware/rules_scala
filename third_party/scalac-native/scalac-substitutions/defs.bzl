load("@rules_java//java:defs.bzl", "JavaInfo")

def _remove_javainfo_dependencies_impl(ctx):
    old_java_info = ctx.attr.src[JavaInfo]

    return [
        ctx.attr.src[DefaultInfo],
        JavaInfo(
            output_jar = old_java_info.runtime_output_jars[0],
            compile_jar = old_java_info.compile_jars.to_list()[0],
            source_jar = old_java_info.source_jars[0],
        ),
    ]

remove_javainfo_dependencies = rule(
    attrs = {
        "src": attr.label(
            doc = "The target whose `JavaInfo` provider to remove dependencies from.",
            mandatory = True,
            providers = [JavaInfo],
        ),
    },
    implementation = _remove_javainfo_dependencies_impl,
)
