load("@rules_pkg//pkg:pkg.bzl", "pkg_tar")
load("@stardoc//stardoc:stardoc.bzl", "stardoc")
load(":stardoc.bzl", "stardoc_targets")

[
    stardoc(
        name = stardoc_target.name,
        out = stardoc_target.out,
        input = stardoc_target.input,
        deps = ["//rules:scala-rules"],
    )
    for stardoc_target in stardoc_targets
]

pkg_tar(
    name = "docs",
    srcs = [stardoc_target.out for stardoc_target in stardoc_targets],
    mode = "0644",
)
