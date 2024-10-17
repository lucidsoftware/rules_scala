workspace(name = "rules_scala_annex")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# rules_license
rules_license_tag = "1.0.0"

http_archive(
    name = "rules_license",
    sha256 = "26d4021f6898e23b82ef953078389dd49ac2b5618ac564ade4ef87cced147b38",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_license/releases/download/{tag}/rules_license-{tag}.tar.gz".format(tag = rules_license_tag),
        "https://github.com/bazelbuild/rules_license/releases/download/{tag}/rules_license-{tag}.tar.gz".format(tag = rules_license_tag),
    ],
)

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

# rules_java
http_archive(
    name = "rules_java",
    sha256 = "dfbadbb37a79eb9e1cc1e156ecb8f817edf3899b28bc02410a6c1eb88b1a6862",
    urls = [
        "https://github.com/bazelbuild/rules_java/releases/download/7.12.1/rules_java-7.12.1.tar.gz",
    ],
)

load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")

rules_java_dependencies()

register_toolchains("//:repository_default_toolchain_21_definition")

# com_github_bazelbuild_buildtools

buildtools_tag = "7.3.1"

buildtools_sha256 = "118602587d5804c720c1617db30f56c93ec7a2bdda5e915125fccf7421e78412"

http_archive(
    name = "com_github_bazelbuild_buildtools",
    sha256 = buildtools_sha256,
    strip_prefix = "buildtools-{}".format(buildtools_tag),
    url = "https://github.com/bazelbuild/buildtools/archive/v{}.zip".format(buildtools_tag),
)

load("@com_github_bazelbuild_buildtools//buildifier:deps.bzl", "buildifier_dependencies")

buildifier_dependencies()

# rules_go

rules_go_tag = "v0.43.0"

rules_go_sha256 = "d6ab6b57e48c09523e93050f13698f708428cfd5e619252e369d377af6597707"

http_archive(
    name = "io_bazel_rules_go",
    sha256 = rules_go_sha256,
    urls = [
        "https://storage.googleapis.com/bazel-mirror/github.com/bazelbuild/rules_go/releases/download/{tag}/rules_go-{tag}.zip".format(tag = rules_go_tag),
        "https://github.com/bazelbuild/rules_go/releases/download/{tag}/rules_go-{tag}.zip".format(tag = rules_go_tag),
    ],
)

load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")

go_rules_dependencies()

go_register_toolchains(version = "1.17")

# rules_jvm_external
rules_jvm_external_tag = "6.4"

http_archive(
    name = "rules_jvm_external",
    sha256 = "8c92f7c7a57273c692da459f70bd72464c87442e86b9e0b495950a7c554c254f",
    strip_prefix = "rules_jvm_external-{}".format(rules_jvm_external_tag),
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/{}.zip".format(rules_jvm_external_tag),
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

# Scala 2_13 and 3
load("//rules/scala:workspace.bzl", "scala_register_toolchains", "scala_repositories")

scala_repositories()

load("@annex//:defs.bzl", annex_pinned_maven_install = "pinned_maven_install")

annex_pinned_maven_install()

scala_register_toolchains(default_scala_toolchain_name = "zinc_2_13")

#  Scala 2.12

load("//rules/scala:workspace_2_12.bzl", "scala_2_12_repositories")

scala_2_12_repositories()

load("@annex_2_12//:defs.bzl", annex_2_12_pinned_maven_install = "pinned_maven_install")

annex_2_12_pinned_maven_install()

# Scala fmt

load("//rules/scalafmt:workspace.bzl", "scalafmt_default_config", "scalafmt_repositories")

scalafmt_repositories()

load("@annex_scalafmt//:defs.bzl", annex_scalafmt_pinned_maven_install = "pinned_maven_install")

annex_scalafmt_pinned_maven_install()

scalafmt_default_config(".scalafmt.conf")

# Scala proto

load(
    "//rules/scala_proto:workspace.bzl",
    "scala_proto_register_toolchains",
    "scala_proto_repositories",
)

scala_proto_repositories()

scala_proto_register_toolchains()

load("@annex_proto//:defs.bzl", annex_proto_pinned_maven_install = "pinned_maven_install")

annex_proto_pinned_maven_install()

# protobuf

protobuf_tag = "3.19.6"

protobuf_sha256 = "387e2c559bb2c7c1bc3798c4e6cff015381a79b2758696afcbf8e88730b47389"

http_archive(
    name = "com_google_protobuf",
    sha256 = protobuf_sha256,
    strip_prefix = "protobuf-{}".format(protobuf_tag),
    type = "zip",
    url = "https://github.com/protocolbuffers/protobuf/archive/v{}.zip".format(protobuf_tag),
)

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

# rules_pkg

rules_pkg_version = "1.0.1"

http_archive(
    name = "rules_pkg",
    sha256 = "d20c951960ed77cb7b341c2a59488534e494d5ad1d30c4818c736d57772a9fef",
    urls = [
        "https://github.com/bazelbuild/rules_pkg/releases/download/{v}/rules_pkg-{v}.tar.gz".format(v = rules_pkg_version),
    ],
)

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")

rules_pkg_dependencies()

# rules_python - this is needed by rules_jvm_external for some reason
rules_python_tag = "0.36.0"

http_archive(
    name = "rules_python",
    sha256 = "ca77768989a7f311186a29747e3e95c936a41dffac779aff6b443db22290d913",
    strip_prefix = "rules_python-{}".format(rules_python_tag),
    url = "https://github.com/bazelbuild/rules_python/releases/download/{}/rules_python-{}.tar.gz".format(rules_python_tag, rules_python_tag),
)

load("@rules_python//python:repositories.bzl", "py_repositories")

py_repositories()
