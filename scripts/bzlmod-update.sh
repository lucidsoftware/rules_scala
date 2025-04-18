#!/bin/env bash
set -euxo pipefail

bazel mod deps --lockfile_mode=update
bazel mod tidy

cd "$(dirname "$0")/../tests"
bazel mod deps --lockfile_mode=update
bazel mod tidy
