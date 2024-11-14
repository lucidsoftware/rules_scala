load("@rules_scala_annex//rules:scala.bzl", "scala_library", "scala_test")

scala_library(
    name = "cancel-spec-worker",
    srcs = [
        "RunnerForCancelSpec.scala",
    ],
    scala_toolchain_name = "test_zinc_2_13",
    tags = ["manual"],
    deps = [
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/sandbox",
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/worker",
    ],
)

scala_test(
    name = "cancel-spec",
    srcs = ["CancelSpec.scala"],
    scala_toolchain_name = "test_zinc_2_13",
    tags = ["manual"],
    deps = [
        ":cancel-spec-worker",
        "//test-lib/worker",
        "@annex_test//:org_scalactic_scalactic_2_13",
        "@annex_test//:org_scalatest_scalatest_compatible",
        "@annex_test//:org_scalatest_scalatest_core_2_13",
        "@annex_test//:org_scalatest_scalatest_flatspec_2_13",
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/error",
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/worker",
        "@rules_scala_annex//third_party/bazel/src/main/protobuf:worker_protocol_java_proto",
    ],
)
