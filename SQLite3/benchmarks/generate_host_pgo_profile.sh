#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIRECTORY=$(cd "$(dirname "$0")" && pwd)
SQLITE3_DIRECTORY=$(cd "$SCRIPT_DIRECTORY/.." && pwd)
OUTPUT_DIRECTORY=${1:?Usage: generate_host_pgo_profile.sh OUTPUT_DIRECTORY TARGET_ABI}
TARGET_ABI=${2:?Usage: generate_host_pgo_profile.sh OUTPUT_DIRECTORY TARGET_ABI}

HOST_SYSTEM=$(uname -s)
case "$HOST_SYSTEM" in
  Darwin|Linux) ;;
  *)
    echo "Host PGO training is supported only on macOS and Linux." >&2
    exit 1
    ;;
esac

if [[ -z ${JAVA_HOME:-} ]]; then
  if [[ "$HOST_SYSTEM" == Darwin ]]; then
    JAVA_HOME=$(/usr/libexec/java_home -v 17)
  else
    JAVAC_EXECUTABLE=$(command -v javac || true)
    if [[ -z "$JAVAC_EXECUTABLE" ]]; then
      echo "Host PGO training requires a JDK or JAVA_HOME." >&2
      exit 1
    fi
    JAVA_HOME=$(cd "$(dirname "$(readlink -f "$JAVAC_EXECUTABLE")")/.." && pwd)
  fi
  export JAVA_HOME
fi

C_COMPILER=${CC:-clang}
CXX_COMPILER=${CXX:-clang++}
if ! "$C_COMPILER" --version | head -n 1 | grep -q clang; then
  echo "Host PGO training requires Clang; CC is '$C_COMPILER'." >&2
  exit 1
fi

LLVM_PROFDATA=${LLVM_PROFDATA:-}
if [[ -z "$LLVM_PROFDATA" ]]; then
  compiler_directory=$(cd "$(dirname "$(command -v "$C_COMPILER")")" && pwd)
  candidates=("$compiler_directory/llvm-profdata")
  if [[ "$HOST_SYSTEM" == Darwin ]]; then
    candidates+=("$(xcrun --find llvm-profdata)")
  fi
  candidates+=(llvm-profdata)
  for candidate in "${candidates[@]}"; do
    if command -v "$candidate" >/dev/null 2>&1; then
      LLVM_PROFDATA=$(command -v "$candidate")
      break
    fi
  done
fi
if [[ -z "$LLVM_PROFDATA" || ! -x "$LLVM_PROFDATA" ]]; then
  echo "llvm-profdata matching '$C_COMPILER' is required." >&2
  exit 1
fi

mkdir -p "$OUTPUT_DIRECTORY"
TRAINING_DIRECTORY=$(mktemp -d "$OUTPUT_DIRECTORY/training.XXXXXX")
trap 'rm -rf "$TRAINING_DIRECTORY"' EXIT
BUILD_DIRECTORY="$TRAINING_DIRECTORY/build"
RAW_PROFILE_PATTERN="$TRAINING_DIRECTORY/sqlite-%p.profraw"
MERGED_PROFILE="$OUTPUT_DIRECTORY/sqlite.profdata"

cmake \
  -S "$SQLITE3_DIRECTORY" \
  -B "$BUILD_DIRECTORY" \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_C_COMPILER="$C_COMPILER" \
  -DCMAKE_CXX_COMPILER="$CXX_COMPILER" \
  -DSLKT_TARGET_ABI="$TARGET_ABI" \
  -DSELEKT_BUILD_NATIVE_BENCHMARKS=ON \
  -DSELEKT_SQLITE_PGO=GENERATE \
  -DSELEKT_ENABLE_THINLTO=OFF
cmake --build "$BUILD_DIRECTORY" --target selekt_native_benchmark --parallel

if [[ "$HOST_SYSTEM" == Darwin ]]; then
  LLVM_PROFILE_FILE="$RAW_PROFILE_PATTERN" \
  DYLD_LIBRARY_PATH="$BUILD_DIRECTORY/sqlite3${DYLD_LIBRARY_PATH:+:$DYLD_LIBRARY_PATH}" \
    "$BUILD_DIRECTORY/sqlite3/selekt_native_benchmark"
else
  LLVM_PROFILE_FILE="$RAW_PROFILE_PATTERN" \
  LD_LIBRARY_PATH="$BUILD_DIRECTORY/sqlite3${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}" \
    "$BUILD_DIRECTORY/sqlite3/selekt_native_benchmark"
fi

RAW_PROFILES=()
while IFS= read -r raw_profile; do
  RAW_PROFILES+=("$raw_profile")
done < <(find "$TRAINING_DIRECTORY" -name '*.profraw' -type f -print)
if [[ ${#RAW_PROFILES[@]} -eq 0 ]]; then
  echo "The instrumented training run produced no .profraw files." >&2
  exit 1
fi
"$LLVM_PROFDATA" merge -output="$MERGED_PROFILE" "${RAW_PROFILES[@]}"
echo "Generated $HOST_SYSTEM SQLite PGO profile: $MERGED_PROFILE"
