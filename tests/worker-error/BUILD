load("@rules_scala_annex//rules:scala.bzl", "scala_binary", "scala_library", "scala_test")

scala_library(
    name = "fatal-error-spec-workers",
    srcs = [
        "RunnerThatThrowsError.scala",
        "RunnerThatThrowsException.scala",
        "RunnerThatThrowsFatalError.scala",
    ],
    scala_version = "2.13",
    deps = [
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/worker",
    ],
)

scala_test(
    name = "error-spec",
    srcs = ["ErrorSpec.scala"],
    scala_version = "2.13",
    deps = [
        ":fatal-error-spec-workers",
        "//test-lib/worker",
        "@annex_test//:org_scalactic_scalactic_2_13",
        "@annex_test//:org_scalatest_scalatest_compatible",
        "@annex_test//:org_scalatest_scalatest_core_2_13",
        "@annex_test//:org_scalatest_scalatest_flatspec_2_13",
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/worker",
        "@rules_scala_annex//third_party/bazel/src/main/protobuf:worker_protocol_java_proto",
    ],
)

scala_test(
    name = "fatal-error-spec",
    srcs = ["FatalErrorSpec.scala"],
    scala_version = "2.13",
    deps = [
        ":fatal-error-spec-workers",
        "//test-lib/worker",
        "@annex_test//:org_scalactic_scalactic_2_13",
        "@annex_test//:org_scalatest_scalatest_core_2_13",
        "@annex_test//:org_scalatest_scalatest_flatspec_2_13",
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/error",
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/worker",
        "@rules_scala_annex//third_party/bazel/src/main/protobuf:worker_protocol_java_proto",
    ],
)

scala_binary(
    name = "worker-throwing-annexworkererror",
    srcs = ["WorkerThrowingAnnexWorkerError.scala"],
    deps = [
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/error",
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/worker",
    ],
)
