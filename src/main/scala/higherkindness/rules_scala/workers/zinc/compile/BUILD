load("//rules:scala.bzl", "scala_binary")
load("//rules:scalafmt.bzl", "scala_format_test")

scala_binary(
    name = "compile",
    srcs = glob(["**/*.scala"]),
    main_class = "higherkindness.rules_scala.workers.zinc.compile.ZincRunner",
    scala_toolchain_name = "annex_bootstrap_3",
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/scala/higherkindness/rules_scala/common/error",
        "//src/main/scala/higherkindness/rules_scala/common/interrupt",
        "//src/main/scala/higherkindness/rules_scala/common/worker",
        "//src/main/scala/higherkindness/rules_scala/workers/common",
        "//src/main/scala/sbt/internal/inc/classfile",
        "//third_party/bazel/src/java_tools/buildjar/java/com/google/devtools/build/buildjar/jarhelper",
    ],
)

scala_format_test(
    name = "format",
    srcs = glob(["**/*.scala"]),
)
