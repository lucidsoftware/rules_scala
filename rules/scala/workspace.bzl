load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")
load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

_SRC_FILEGROUP_BUILD_FILE_CONTENT = """
filegroup(
    name = "src",
    srcs = glob(["**/*.scala", "**/*.java"]),
    visibility = ["//visibility:public"]
)"""

scala_2_13_version = "2.13.14"
scala_3_version = "3.5.1"
zinc_version = "1.10.1"

def scala_artifacts():
    return [
        "ch.epfl.scala:bloop-frontend_2.12:1.0.0",
        "com.lihaoyi:sourcecode_2.13:0.2.7,",
        "com.thesamet.scalapb:lenses_2.13:0.11.17",
        "com.thesamet.scalapb:scalapb-runtime_2.13:0.11.17",
        "net.sourceforge.argparse4j:argparse4j:0.8.1",
        "org.jacoco:org.jacoco.core:0.7.5.201505241946",
        "org.scala-sbt:test-interface:1.0",
        "org.scala-sbt:compiler-interface:{}".format(zinc_version),
        "org.scala-sbt:io_2.13:1.10.0",
        "org.scala-sbt:util-interface:{}".format(zinc_version),
        "org.scala-sbt:util-logging_2.13:{}".format(zinc_version),
        "org.scala-sbt:zinc_2.13:{}".format(zinc_version),
        "org.scala-sbt:zinc-apiinfo_2.13:{}".format(zinc_version),
        "org.scala-sbt:zinc-classpath_2.13:{}".format(zinc_version),
        "org.scala-sbt:zinc-compile-core_2.13:{}".format(zinc_version),
        "org.scala-sbt:zinc-core_2.13:{}".format(zinc_version),
        "org.scala-sbt:zinc-persist_2.13:{}".format(zinc_version),
        "org.scala-sbt:compiler-interface:{}".format(zinc_version),
        "org.scala-lang:scala-compiler:{}".format(scala_2_13_version),
        "org.scala-lang:scala-library:{}".format(scala_2_13_version),
        "org.scala-lang:scala-reflect:{}".format(scala_2_13_version),
        "org.scala-lang:scala3-compiler_3:{}".format(scala_3_version),
        "org.scala-lang:scala3-library_3:{}".format(scala_3_version),
        # The compiler bridge has a dependency on compiler-interface, which has a dependency on the Scala 2
        # library. We need to set this to neverlink = True to avoid this the Scala 2 library being pulled
        # onto the Scala 3, and other Scala versions like 2.12, compiler classpath during runtime.
        maven.artifact("org.scala-lang", "scala3-sbt-bridge", scala_3_version, neverlink = True),
        # The compiler bridge has a dependency on compiler-interface, which has a dependency on the Scala 2
        # library. We need to set this to neverlink = True to avoid this the Scala 2 library being pulled
        # onto the Scala 3, and other Scala versions like 2.12, compiler classpath during runtime.
        maven.artifact("org.scala-sbt", "compiler-bridge_2.13", zinc_version, neverlink = True),
    ]

def scala_repositories(
        java_launcher_version = "7.2.0",
        java_launcher_template_sha = "ee4aa47ae5e639632c67be5cc0ccbc4e941a67a1b884a1ce0c4329357a4b62b2"):
    maven_install(
        name = "annex",
        artifacts = scala_artifacts(),
        repositories = [
            "https://repo.maven.apache.org/maven2",
            "https://maven-central.storage-download.googleapis.com/maven2",
            "https://mirror.bazel.build/repo1.maven.org/maven2",
        ],
        fetch_sources = True,
        maven_install_json = "@rules_scala_annex//:annex_install.json",
    )

    java_stub_template_url = (
        "raw.githubusercontent.com/bazelbuild/bazel/" +
        java_launcher_version +
        "/src/main/java/com/google/devtools/build/lib/bazel/rules/java/" +
        "java_stub_template.txt"
    )

    http_file(
        name = "anx_java_stub_template",
        sha256 = java_launcher_template_sha,
        urls = [
            "https://%s" % java_stub_template_url,
        ],
    )

def _toolchain_configuration_repository_impl(repository_ctx):
    repository_ctx.file(
        "BUILD",
        """\
load(":default.bzl", "default_scala_toolchain_name")
load("@bazel_skylib//rules:common_settings.bzl", "string_setting")

string_setting(
    name = "scala-toolchain",
    build_setting_default = default_scala_toolchain_name,
    visibility = ["//visibility:public"],
)
""",
    )

    repository_ctx.file(
        "default.bzl",
        "default_scala_toolchain_name = \"{}\"\n".format(
            repository_ctx.attr.default_scala_toolchain_name,
        ),
    )

_toolchain_configuration_repository = repository_rule(
    attrs = {
        "default_scala_toolchain_name": attr.string(mandatory = True),
    },
    doc = "Defines a setting for the Scala toolchain to use. This is done in a separate repository so we can provide the default dynamically.",
    implementation = _toolchain_configuration_repository_impl,
)

def scala_register_toolchains(default_scala_toolchain_name, toolchains = []):
    _toolchain_configuration_repository(
        name = "rules_scala_annex_scala_toolchain",
        default_scala_toolchain_name = default_scala_toolchain_name,
    )

    native.register_toolchains(
        "//src/main/scala:bootstrap_2_13",
        "//src/main/scala:bootstrap_3",
        "//src/main/scala:zinc_2_13",
        "//src/main/scala:zinc_3",
        *toolchains
    )
