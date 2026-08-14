load("@rules_scala_annex//rules:scala.bzl", "scala_library", "scala_test")

scala_library(
    name = "worker",
    srcs = glob(["*.scala"]),
    scala_version = "2.13",
    visibility = ["//visibility:public"],
    deps = [
        "@rules_scala_annex//third_party/bazel/src/main/protobuf:worker_protocol_java_proto",
    ],
)
