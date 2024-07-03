load("//rules:scala.bzl", "scala_library")
load("//rules:scalafmt.bzl", "scala_format_test")

scala_library(
    name = "worker",
    srcs = glob(["**/*.scala"]),
    scala = "//src/main/scala:bootstrap",
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/scala/higherkindness/rules_scala/common/error",
        "//third_party/bazel/src/main/protobuf:worker_protocol_java_proto",
    ],
)

scala_format_test(
    name = "format",
    srcs = glob(["**/*.scala"]),
)
