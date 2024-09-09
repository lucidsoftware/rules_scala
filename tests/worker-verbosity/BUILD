load("@rules_scala_annex//rules:scala.bzl", "scala_binary")
load("verbosity_spec_worker_run.bzl", "verbosity_spec_worker_run")

scala_binary(
    name = "verbosity-spec-worker",
    srcs = [
        "RunnerThatPrintsVerbosity.scala",
    ],
    scala = "//scala:2_13",
    tags = ["manual"],
    deps = [
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/sandbox",
        "@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/worker",
    ],
)

verbosity_spec_worker_run(
    name = "verbosity-spec-target",
    verbosity_spec_worker = ":verbosity-spec-worker",
)
