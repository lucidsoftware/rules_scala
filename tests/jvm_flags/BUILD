load("@rules_scala_annex//rules:default_bootstrap_toolchain.bzl", "default_bootstrap_toolchain")
load("@rules_scala_annex//rules:register_toolchain.bzl", "register_zinc_toolchain")
load("@rules_scala_annex//rules:scala.bzl", "scala_library")
load("@rules_scala_annex//rules:scalafmt.bzl", "scala_format_test")
load("@rules_scala_annex//rules/scala:versions.bzl", "scala_3_version")
load("@rules_scala_annex//rules/scalafmt:register_toolchain.bzl", "register_scalafmt_toolchain")

bad_jvm_flags = ["-XX:+AnnexDefinitelyNotARealJvmFlag"]

good_jvm_flags = [
    "-Dannex.jvm_flags.test=enabled",
    "-XshowSettings:properties",
]

compiler_classpath_3 = [
    "@annex_test//:org_scala_lang_scala3_compiler_3",
    "@annex_test//:org_scala_lang_scala3_library_3",
]

runtime_classpath_3 = [
    "@annex_test//:org_scala_lang_scala3_library_3",
    "@annex_test//:org_scala_lang_scala3_interfaces",
    "@annex_test//:org_scala_lang_tasty_core_3",
]

default_bootstrap_toolchain(
    name = "bootstrap_3_bad_jvm_flags",
    jvm_flags = bad_jvm_flags,
    prefix = "jvm-flags-bad-bootstrap",
)

default_bootstrap_toolchain(
    name = "bootstrap_3_good_jvm_flags",
    jvm_flags = good_jvm_flags,
    prefix = "jvm-flags-good-bootstrap",
)

register_zinc_toolchain(
    name = "zinc_3_bad_jvm_flags",
    compiler_bridge = "@annex_test//:org_scala_lang_scala3_sbt_bridge",
    compiler_classpath = compiler_classpath_3,
    jvm_flags = bad_jvm_flags,
    prefix = "jvm-flags-bad-zinc",
    runtime_classpath = runtime_classpath_3,
    use_ijar = True,
    version = scala_3_version,
    visibility = ["//visibility:public"],
)

register_zinc_toolchain(
    name = "zinc_3_good_jvm_flags",
    compiler_bridge = "@annex_test//:org_scala_lang_scala3_sbt_bridge",
    compiler_classpath = compiler_classpath_3,
    jvm_flags = good_jvm_flags,
    prefix = "jvm-flags-good-zinc",
    runtime_classpath = runtime_classpath_3,
    use_ijar = True,
    version = scala_3_version,
    visibility = ["//visibility:public"],
)

register_scalafmt_toolchain(
    name = "scalafmt_3_bad_jvm_flags",
    config = "//:.scalafmt.conf",
    jvm_flags = bad_jvm_flags,
    scala_versions = ["jvm-flags-bad-scalafmt_" + scala_3_version],
    visibility = ["//visibility:public"],
)

register_scalafmt_toolchain(
    name = "scalafmt_3_good_jvm_flags",
    config = "//:.scalafmt.conf",
    jvm_flags = good_jvm_flags,
    scala_versions = ["jvm-flags-good-scalafmt_" + scala_3_version],
    visibility = ["//visibility:public"],
)

scala_library(
    name = "bootstrap_good",
    srcs = ["Example.scala"],
    scala_version = "jvm-flags-good-bootstrap_3",
)

scala_library(
    name = "bootstrap_bad",
    srcs = ["Example.scala"],
    scala_version = "jvm-flags-bad-bootstrap_3",
    tags = ["manual"],
)

scala_library(
    name = "zinc_good",
    srcs = ["Example.scala"],
    scala_version = "jvm-flags-good-zinc_3",
)

scala_library(
    name = "zinc_bad",
    srcs = ["Example.scala"],
    scala_version = "jvm-flags-bad-zinc_3",
    tags = ["manual"],
)

scala_format_test(
    name = "scalafmt_good",
    srcs = ["Example.scala"],
    scala_version = "jvm-flags-good-scalafmt_3",
)

scala_format_test(
    name = "scalafmt_bad",
    srcs = ["Example.scala"],
    scala_version = "jvm-flags-bad-scalafmt_3",
    tags = ["manual"],
)
