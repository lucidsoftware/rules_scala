#!/bin/bash -e

#
# Runs the integration test suite.
#

set -o pipefail
cd "$(dirname "$0")/.."

# Run ordinary Bazel tests together. The scripts below cover custom invocations and expected failures.
(cd tests && bazel test --build_tests_only --test_output=errors //...)

find tests -name test -type f | sort | while read f; do
    echo running ${f#"tests/"}...
    output=$($f 2>&1) || ( echo "$output" && exit 1 )
done

trap echo ERR
