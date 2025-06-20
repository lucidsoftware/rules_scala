#!/usr/bin/env bash

JAVA_HOME="$1"

mkdir temporary

"$JAVA_HOME/bin/jimage" extract --dir temporary "$JAVA_HOME/lib/modules"

for argument in "${@:2}"; do
    IFS=: read -r module output_jar <<< "$argument"

    "$JAVA_HOME/bin/jar" cf "$output_jar" -C "temporary/$module" .
done
