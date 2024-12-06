#!/usr/bin/env bash

set -e

#
# Regenerates Stardoc for the rules
#

cd "$(dirname "$0")/.."
set -x

rm -fr docs/stardoc
mkdir -p docs/stardoc
bazel build //dev/stardoc:docs
tar xf "$(bazel info bazel-bin)/dev/stardoc/docs.tar" -C docs/stardoc
