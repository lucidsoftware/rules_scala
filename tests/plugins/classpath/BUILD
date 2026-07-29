load("@rules_java//java:defs.bzl", "java_binary")
load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(
    name = "plugin-lib",
    srcs = ["plugin-lib.scala"],
    scala_version = "2.13",
    tags = ["manual"],
)

_gen_plugin_xml_cmd = """
cat > $@ << EOF
<plugin>
  <name>plugin</name>
  <classname>plug.it.in.Plugin</classname>
</plugin>
"""

genrule(
    name = "gen-scalac-plugin.xml",
    outs = ["scalac-plugin.xml"],
    cmd = _gen_plugin_xml_cmd,
)

java_binary(
    name = "resources",
    classpath_resources = [
        ":gen-scalac-plugin.xml",
    ],
    create_executable = False,
)

scala_library(
    name = "plugin",
    srcs = ["plugin.scala"],
    resource_jars = [
        ":resources",
    ],
    scala_version = "2.13",
    tags = ["manual"],
    deps = [
        ":plugin-lib",
        "@annex_test//:org_scala_lang_scala_compiler",
        "@annex_test//:org_scala_lang_scala_reflect",
    ],
)

scala_library(
    name = "inefficient-usage",
    srcs = ["usage.scala"],
    plugins = [
        ":plugin",
    ],
    scala_version = "2.13",
    tags = ["manual"],
)
