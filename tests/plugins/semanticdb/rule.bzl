load("@rules_scala_annex//rules:providers.bzl", "SemanticDbInfo")

def _read_semanticdb_info_impl(ctx):
    semanticdb_info = ctx.attr.scala_target[SemanticDbInfo]
    output = ctx.actions.declare_file("{}.txt".format(ctx.label.name))

    ctx.actions.write(
        output,
        json.encode({
            "targetRoot": semanticdb_info.target_root,
            "semanticDbFiles": sorted([file.path for file in semanticdb_info.semanticdb_files]),
        }),
    )

    return DefaultInfo(files = depset([output]))

read_semanticdb_info = rule(
    attrs = {
        "scala_target": attr.label(
            mandatory = True,
            providers = [SemanticDbInfo],
        ),
    },
    implementation = _read_semanticdb_info_impl,
)
