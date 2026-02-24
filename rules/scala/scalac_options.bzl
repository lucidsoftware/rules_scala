"""Scalac options for annex's Scala toolchains."""

shared_global_scalacopts = [
    "-deprecation",
    "-Wconf:any:error",
]

shared_scala2_global_scalacopts = [
    "-Xlint:_,-unused",
    "-Ytasty-reader",
    "-Xsource:3",
]

scala2_global_scalacopts = shared_global_scalacopts + shared_scala2_global_scalacopts
