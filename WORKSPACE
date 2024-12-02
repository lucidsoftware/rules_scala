workspace(name = "rules_scala_annex")

# Scala 2_13 and 3
load("//rules/scala:workspace.bzl", "scala_register_toolchains")

scala_register_toolchains(default_scala_toolchain_name = "annex_zinc_2_13")

# Scala fmt

load("//rules/scalafmt:workspace.bzl", "scalafmt_default_config")

scalafmt_default_config(".scalafmt.conf")

# Scala proto

load("//rules/scala_proto:workspace.bzl", "scala_proto_register_toolchains")

scala_proto_register_toolchains()
