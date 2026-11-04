#!/usr/bin/env bash

set -euo pipefail

usage() {
  echo "Usage: $0 <arguments_file>

Required arguments:
  --output <file>

Optional arguments:
  --compile_classpath [<jar>...]
  --compile_classpath_short_paths [<short_path>...]
  --compile_runtime_jar_pairs [<compile_jar>:<runtime_jar>...]
  --used_jar_list <file>
  --used_jars [<jar>...]
  -h|--help"
}

mapfile -t arguments < "${1#@}"
set -- "${arguments[@]}"

COMPILE_CLASSPATH=()
COMPILE_CLASSPATH_SHORT_PATHS=()
COMPILE_RUNTIME_JAR_PAIRS=()
OUTPUT_FILE=
USED_JAR_LISTS=()
USED_JARS=()

while [[ $# -gt 0 ]]; do
    case "$1" in
        --compile_classpath)
          shift

          while [[ $# -gt 0 && ! "$1" =~ ^- ]]; do
            COMPILE_CLASSPATH+=("$1")
            shift
          done

          ;;
        --compile_classpath_short_paths)
          shift

          while [[ $# -gt 0 && ! "$1" =~ ^- ]]; do
            COMPILE_CLASSPATH_SHORT_PATHS+=("$1")
            shift
          done

          ;;
        --compile_runtime_jar_pairs)
          shift

          while [[ $# -gt 0 && ! "$1" =~ ^- ]]; do
            COMPILE_RUNTIME_JAR_PAIRS+=("$1")
            shift
          done

          ;;
        --used_jar_list)
          shift
          USED_JAR_LISTS+=("$1")
          shift
          ;;
        --used_jars)
          shift

          while [[ $# -gt 0 && ! "$1" =~ ^- ]]; do
            USED_JARS+=("$1")
            shift
          done

          ;;
        --output)
          shift
          OUTPUT_FILE="$1"
          shift
          ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1" >&2
            usage
            exit 1
            ;;
    esac
done

if [[ -z "$OUTPUT_FILE" ]]; then
    echo "Error: --output is required" >&2
    usage
    exit 1
fi

if [[ ${#COMPILE_CLASSPATH[@]} -ne ${#COMPILE_CLASSPATH_SHORT_PATHS[@]} ]]; then
    echo "Error: Expected one short path per compile classpath entry." >&2
    exit 1
fi

declare -A RETAINED_JARS=()

for used_jar in "${USED_JARS[@]}"; do
  RETAINED_JARS["$used_jar"]=1
done

for used_jar_list in "${USED_JAR_LISTS[@]}"; do
  while IFS= read -r jar; do
    RETAINED_JARS["$jar"]=1
  done < "$used_jar_list"
done

for pair in "${COMPILE_RUNTIME_JAR_PAIRS[@]}"; do
  IFS=':' read -r compile_jar runtime_jar <<< "$pair"

  if [[ -v RETAINED_JARS["$compile_jar"] ]]; then
    RETAINED_JARS["$runtime_jar"]=1
  elif [[ -v RETAINED_JARS["$runtime_jar"] ]]; then
    RETAINED_JARS["$compile_jar"]=1
  fi
done

# Compare JARs by their short paths, but output the full input paths Bazel expects `unused_inputs_list` to contain
{
  for i in "${!COMPILE_CLASSPATH[@]}"; do
    if [[ ! -v RETAINED_JARS["${COMPILE_CLASSPATH_SHORT_PATHS[$i]}"] ]]; then
      printf '%s\n' "${COMPILE_CLASSPATH[$i]}"
    fi
  done
} | LC_ALL=C sort -u > "$OUTPUT_FILE"
