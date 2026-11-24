load("@rules_scala_annex//rules:scala.bzl", "scala_test")

scala_test(
    name = "specs",
    srcs = glob(["*.scala"]),
    scala_version = "3",
    runtime_deps = [
        "@annex_test//:org_hamcrest_hamcrest",
    ],
    deps = [
        "@annex_test//:com_github_sbt_junit_interface",
        "@annex_test//:junit_junit",
        "@protobuf//java/core",
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/workers/common",
        "@rules_scala_annex//third_party/bazel/src/main/protobuf:worker_protocol_java_proto",
    ],
)
