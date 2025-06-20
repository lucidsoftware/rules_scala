load("@rules_scala_annex//rules:scala.bzl", "scala_binary", "scala_library")

scala_library(
    name = "library",
    srcs = ["Library.scala"],
    scala_toolchain_name = "test_native_2_13",
)

scala_binary(
    name = "native",
    srcs = ["Main.scala"],
    main_class = "Main",
    scala_toolchain_name = "test_native_2_13",
    deps = [":library"],
)
