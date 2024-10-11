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
    sha256 = "a9690bc00c538246880d5c83c233e4deb83fe885f54c21bb445eb8116a180b83",
    urls = [
        "https://github.com/bazelbuild/rules_java/releases/download/7.12.2/rules_java-7.12.2.tar.gz",
    ],
)

load("@rules_java//java:repositories.bzl", "rules_java_dependencies")

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

# rules_cc

http_archive(
    name = "rules_cc",
    sha256 = "bbf1ae2f83305b7053b11e4467d317a7ba3517a12cef608543c1b1c5bf48a4df",
    strip_prefix = "rules_cc-0.0.16",
    urls = ["https://github.com/bazelbuild/rules_cc/releases/download/0.0.16/rules_cc-0.0.16.tar.gz"],
)

# rules_jvm_external
rules_jvm_external_tag = "6.5"

http_archive(
    name = "rules_jvm_external",
    sha256 = "c5b27928eeb8f0761f0805540587660f0ecc6946e9c04bf0d4c89e2f7d332b2b",
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

protobuf_tag = "28.3"

protobuf_sha256 = "5b2ff0f72e85dc1350b7bb1b4ea94d7e92e297f7a58b630e46fa6b430b5b253b"

http_archive(
    name = "com_google_protobuf",
    sha256 = protobuf_sha256,
    strip_prefix = "protobuf-{}".format(protobuf_tag),
    type = "zip",
    url = "https://github.com/protocolbuffers/protobuf/archive/v{}.zip".format(protobuf_tag),
)

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

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

# Stardoc

http_archive(
    name = "io_bazel_stardoc",
    sha256 = "fabb280f6c92a3b55eed89a918ca91e39fb733373c81e87a18ae9e33e75023ec",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/stardoc/releases/download/0.7.1/stardoc-0.7.1.tar.gz",
        "https://github.com/bazelbuild/stardoc/releases/download/0.7.1/stardoc-0.7.1.tar.gz",
    ],
)

load("@io_bazel_stardoc//:setup.bzl", "stardoc_repositories")

stardoc_repositories()

load("@io_bazel_stardoc//:deps.bzl", "stardoc_external_deps")

stardoc_external_deps()

load("@stardoc_maven//:defs.bzl", stardoc_pinned_maven_install = "pinned_maven_install")

stardoc_pinned_maven_install()

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
