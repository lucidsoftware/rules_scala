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

# com_github_bazelbuild_buildtools

buildtools_tag = "7.1.2"

buildtools_sha256 = "4c63e823e6944c950401f92155416c631a65657dd32e1021451fc015faf22ecb"

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

# io_bazel_rules_go

rules_go_tag = "v0.43.0"

rules_go_sha256 = "8e968b5fcea1d2d64071872b12737bbb5514524ee5f0a4f54f5920266c261acb"

http_archive(
    name = "io_bazel_rules_go",
    integrity = "sha256-1qtrV+SMCVI+kwUPE2mPcIQoz9XmGSUuNp03evZZdwc=",
    urls = [
        "https://storage.googleapis.com/bazel-mirror/github.com/bazelbuild/rules_go/releases/download/{tag}/rules_go-{tag}.zip".format(tag = rules_go_tag),
        "https://github.com/bazelbuild/rules_go/releases/download/{tag}/rules_go-{tag}.zip".format(tag = rules_go_tag),
    ],
)

load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")

go_rules_dependencies()

go_register_toolchains(version = "1.17")

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

# rules_jvm_external
rules_jvm_external_tag = "6.1"

http_archive(
    name = "rules_jvm_external",
    sha256 = "42a6d48eb2c08089961c715a813304f30dc434df48e371ebdd868fc3636f0e82",
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

scala_register_toolchains()

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

rules_pkg_version = "1.0.0"

http_archive(
    name = "rules_pkg",
    sha256 = "cad05f864a32799f6f9022891de91ac78f30e0fa07dc68abac92a628121b5b11",
    urls = [
        "https://github.com/bazelbuild/rules_pkg/releases/download/{v}/rules_pkg-{v}.tar.gz".format(v = rules_pkg_version),
    ],
)

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")

rules_pkg_dependencies()

# rules_python - this is needed by rules_jvm_external for some reason
rules_python_tag = "0.33.2"

http_archive(
    name = "rules_python",
    sha256 = "e3f1cc7a04d9b09635afb3130731ed82b5f58eadc8233d4efb59944d92ffc06f",
    strip_prefix = "rules_python-{}".format(rules_python_tag),
    url = "https://github.com/bazelbuild/rules_python/releases/download/{}/rules_python-{}.tar.gz".format(rules_python_tag, rules_python_tag),
)

load("@rules_python//python:repositories.bzl", "py_repositories")

py_repositories()
