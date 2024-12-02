load("@bazel_tools//tools/build_defs/repo:local.bzl", "new_local_repository")

def scalafmt_default_config(path = ".scalafmt.conf"):
    build = []
    build.append("filegroup(")
    build.append("    name = \"config\",")
    build.append("    srcs = [\"{}\"],".format(path))
    build.append("    visibility = [\"//visibility:public\"],")
    build.append(")")
    new_local_repository(name = "scalafmt_default", build_file_content = "\n".join(build), path = "")
