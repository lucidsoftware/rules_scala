#!/bin/sh -e

#
# Regenerates the external dependencies lock file using rules_jvm_external
#

cd "$(dirname "$0")/.."
echo "$(dirname "$0")/.."

echo "generating dependencies for main workspace"
bazel run @annex//:pin
bazel run @annex_scalafmt//:pin
bazel run @annex_proto//:pin

echo "generating dependencies for tests workspace"
cd "tests"
bazel run @annex_test//:pin
bazel run @annex_test_2_12//:pin
