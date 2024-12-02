workspace(name = "rules_scala_annex")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# skylib

bazel_skylib_tag = "1.7.1"

bazel_skylib_sha256 = "bc283cdfcd526a52c3201279cda4bc298652efa898b10b4db0837dc51652756f"

http_archive(
    name = "bazel_skylib",
    sha256 = bazel_skylib_sha256,
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/{tag}/bazel-skylib-{tag}.tar.gz".format(tag = bazel_skylib_tag),
        "https://github.com/bazelbuild/bazel-skylib/releases/download/{tag}/bazel-skylib-{tag}.tar.gz".format(tag = bazel_skylib_tag),
    ],
)

load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")

bazel_skylib_workspace()

# Scala 2_13 and 3
load("//rules/scala:workspace.bzl", "scala_register_toolchains")

scala_register_toolchains(default_scala_toolchain_name = "annex_zinc_2_13")

# Scala fmt

load("//rules/scalafmt:workspace.bzl", "scalafmt_default_config")

scalafmt_default_config(".scalafmt.conf")

# Scala proto

load("//rules/scala_proto:workspace.bzl", "scala_proto_register_toolchains")

scala_proto_register_toolchains()

# rules_proto

http_archive(
    name = "rules_proto",
    sha256 = "0e5c64a2599a6e26c6a03d6162242d231ecc0de219534c38cb4402171def21e8",
    strip_prefix = "rules_proto-7.0.2",
    url = "https://github.com/bazelbuild/rules_proto/releases/download/7.0.2/rules_proto-7.0.2.tar.gz",
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies")

rules_proto_dependencies()

load("@rules_proto//proto:setup.bzl", "rules_proto_setup")

rules_proto_setup()
