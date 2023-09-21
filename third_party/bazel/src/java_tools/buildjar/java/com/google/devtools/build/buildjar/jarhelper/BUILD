licenses(["notice"])

# Copyright 2014 The Bazel Authors. Licensed under Apache License, Version 2.0
load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
    name = "jarhelper",
    srcs = [
        "JarCreator.java",
        "JarHelper.java",
    ],
    visibility = ["//visibility:public"],
)

java_binary(
    name = "jarcreator_bin",
    main_class = "com.google.devtools.build.buildjar.jarhelper.JarCreator",
    visibility = ["//visibility:public"],
    runtime_deps = [":jarhelper"],
)
