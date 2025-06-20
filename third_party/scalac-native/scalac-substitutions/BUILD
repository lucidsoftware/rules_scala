load("@rules_java//java:defs.bzl", "java_library")
load("//rules:scala.bzl", "scala_library")
load(":defs.bzl", "remove_javainfo_dependencies")

exports_files(
    ["reflection-config.json"],
    visibility = ["//visibility:public"],
)

java_library(
    name = "scalac-java-substitutions",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = ["@annex//:org_graalvm_nativeimage_svm"],
)

scala_library(
    name = "scalac-scala-substitutions",
    srcs = glob(["src/main/scala/**/*.scala"]),
    scala_toolchain_name = "annex_bootstrap_2_13",
    visibility = ["//visibility:public"],
    deps = ["@annex//:org_graalvm_nativeimage_svm"],
)

# We need to remove all the dependencies from `:scalac-*-substitutions-underlying`'s `JavaInfo`. Otherwise, when they're
# included on the classpath to `native-image`, the runtime classpath of the toolchain (the Scala standard library) will
# be included twice. Also, we don't want `@annex//:org_graalvm_nativeimage_svm` on the `native-image` classpath because
# GraalVM will provide that.
remove_javainfo_dependencies(
    name = "scalac-java-substitutions-no-dependencies",
    src = ":scalac-java-substitutions",
    visibility = ["//visibility:public"],
)

remove_javainfo_dependencies(
    name = "scalac-scala-substitutions-no-dependencies",
    src = ":scalac-scala-substitutions",
    visibility = ["//visibility:public"],
)
