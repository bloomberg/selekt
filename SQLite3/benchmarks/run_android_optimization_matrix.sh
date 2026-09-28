#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIRECTORY=$(cd "$(dirname "$0")" && pwd)
SQLITE3_DIRECTORY=$(cd "$SCRIPT_DIRECTORY/.." && pwd)
ROOT_DIRECTORY=$(cd "$SQLITE3_DIRECTORY/.." && pwd)
OUTPUT_DIRECTORY=${1:-"$ROOT_DIRECTORY/build/android-native-benchmark"}
ANDROID_SERIAL_VALUE=${ANDROID_SERIAL:-}
ANDROID_NDK_DIRECTORY=${ANDROID_NDK_HOME:-${ANDROID_NDK_ROOT:-}}
DEVICE_DIRECTORY=/data/local/tmp/selekt-native-benchmark

if [[ -z "$ANDROID_NDK_DIRECTORY" ]]; then
  echo "ANDROID_NDK_HOME or ANDROID_NDK_ROOT must point to Android NDK 27." >&2
  exit 1
fi
if ! command -v adb >/dev/null 2>&1; then
  echo "adb is required to run the benchmark on an ARM64 Android device." >&2
  exit 1
fi

ADB=(adb)
if [[ -n "$ANDROID_SERIAL_VALUE" ]]; then
  ADB+=( -s "$ANDROID_SERIAL_VALUE" )
fi
DEVICE_ABI=$("${ADB[@]}" shell getprop ro.product.cpu.abi | tr -d '\r')
if [[ "$DEVICE_ABI" != "arm64-v8a" ]]; then
  echo "Expected an arm64-v8a device, found '$DEVICE_ABI'." >&2
  exit 1
fi

TOOLCHAIN_FILE="$ANDROID_NDK_DIRECTORY/build/cmake/android.toolchain.cmake"
if [[ ! -f "$TOOLCHAIN_FILE" ]]; then
  echo "Android CMake toolchain not found: $TOOLCHAIN_FILE" >&2
  exit 1
fi
if [[ ! -f "$ROOT_DIRECTORY/OpenSSL/build/libs/arm64-v8a/libcrypto.a" ]]; then
  echo "Build OpenSSL for arm64-v8a before running this script." >&2
  exit 1
fi
if [[ ! -f "$SQLITE3_DIRECTORY/sqlite3/generated/cpp/sqlite3.c" ]]; then
  echo "Generate the SQLCipher amalgamation with ./gradlew :SQLite3:amalgamate first." >&2
  exit 1
fi

case "$(uname -s)" in
  Darwin) NDK_HOST_TAG=darwin-x86_64 ;;
  Linux) NDK_HOST_TAG=linux-x86_64 ;;
  *) echo "Unsupported build host: $(uname -s)" >&2; exit 1 ;;
esac
LLVM_PROFDATA="$ANDROID_NDK_DIRECTORY/toolchains/llvm/prebuilt/$NDK_HOST_TAG/bin/llvm-profdata"
if [[ ! -x "$LLVM_PROFDATA" ]]; then
  echo "llvm-profdata not found: $LLVM_PROFDATA" >&2
  exit 1
fi

mkdir -p "$OUTPUT_DIRECTORY"
"${ADB[@]}" shell "mkdir -p '$DEVICE_DIRECTORY'"

GENERATOR_ARGUMENTS=( -G "Unix Makefiles" )
NINJA_EXECUTABLE=$(command -v ninja || true)
if [[ -z "$NINJA_EXECUTABLE" ]]; then
  ANDROID_SDK_DIRECTORY=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}
  if [[ -z "$ANDROID_SDK_DIRECTORY" ]]; then
    ANDROID_SDK_DIRECTORY=$(cd "$ANDROID_NDK_DIRECTORY/../.." && pwd)
  fi
  NINJA_EXECUTABLE=$(find "$ANDROID_SDK_DIRECTORY/cmake" -path '*/bin/ninja' -type f 2>/dev/null | sort -r | head -n 1)
fi
if [[ -n "$NINJA_EXECUTABLE" ]]; then
  GENERATOR_ARGUMENTS=( -G Ninja -DCMAKE_MAKE_PROGRAM="$NINJA_EXECUTABLE" )
fi

configure_and_build() {
  local name=$1
  local optimization=$2
  local thin_lto=$3
  local pgo_mode=$4
  local profile=${5:-}
  local build_directory="$OUTPUT_DIRECTORY/build-$name"
  local arguments=(
    -S "$SQLITE3_DIRECTORY"
    -B "$build_directory"
    "${GENERATOR_ARGUMENTS[@]}"
    -DCMAKE_BUILD_TYPE=Release
    -DCMAKE_TOOLCHAIN_FILE="$TOOLCHAIN_FILE"
    -DANDROID_ABI=arm64-v8a
    -DANDROID_PLATFORM=android-24
    -DANDROID_STL=c++_static
    -DSLKT_TARGET_ABI=arm64-v8a
    -DSELEKT_BUILD_NATIVE_BENCHMARKS=ON
    -DSELEKT_SQLITE_OPTIMIZATION="$optimization"
    -DSELEKT_ENABLE_THINLTO="$thin_lto"
    -DSELEKT_SQLITE_PGO="$pgo_mode"
  )
  if [[ -n "$profile" ]]; then
    arguments+=( -DSELEKT_SQLITE_PGO_PROFILE="$profile" )
  fi
  cmake "${arguments[@]}"
  cmake --build "$build_directory" --target selekt_native_benchmark
}

run_variant() {
  local name=$1
  local profile_pattern=${2:-}
  local build_directory="$OUTPUT_DIRECTORY/build-$name/sqlite3"
  local device_variant_directory="$DEVICE_DIRECTORY/$name"
  "${ADB[@]}" shell "rm -rf '$device_variant_directory' && mkdir -p '$device_variant_directory'"
  "${ADB[@]}" push "$build_directory/libselekt.so" "$device_variant_directory/libselekt.so" >/dev/null
  "${ADB[@]}" push "$build_directory/selekt_native_benchmark" "$device_variant_directory/selekt_native_benchmark" >/dev/null
  "${ADB[@]}" shell "chmod 755 '$device_variant_directory/selekt_native_benchmark'"
  if [[ -n "$profile_pattern" ]]; then
    "${ADB[@]}" shell \
      "cd '$device_variant_directory' && LD_LIBRARY_PATH=. LLVM_PROFILE_FILE='$profile_pattern' ./selekt_native_benchmark" \
      | tee "$OUTPUT_DIRECTORY/$name.log"
  else
    "${ADB[@]}" shell \
      "cd '$device_variant_directory' && LD_LIBRARY_PATH=. ./selekt_native_benchmark" \
      | tee "$OUTPUT_DIRECTORY/$name.log"
  fi
}

configure_and_build o2 O2 OFF OFF
run_variant o2
configure_and_build o3 O3 OFF OFF
run_variant o3
configure_and_build thinlto O2 ON OFF
run_variant thinlto

configure_and_build pgo-generate O2 OFF GENERATE
run_variant pgo-generate "$DEVICE_DIRECTORY/pgo-generate/sqlite-%p.profraw"
PROFILE_PULL_DIRECTORY="$OUTPUT_DIRECTORY/profiles-$(date +%Y%m%d-%H%M%S)-$$"
mkdir -p "$PROFILE_PULL_DIRECTORY"
"${ADB[@]}" pull "$DEVICE_DIRECTORY/pgo-generate" "$PROFILE_PULL_DIRECTORY" >/dev/null
RAW_PROFILES=()
while IFS= read -r profile; do
  RAW_PROFILES+=( "$profile" )
done < <(find "$PROFILE_PULL_DIRECTORY" -name '*.profraw' -type f -print)
if [[ ${#RAW_PROFILES[@]} -eq 0 ]]; then
  echo "The instrumented run produced no .profraw files." >&2
  exit 1
fi
MERGED_PROFILE="$OUTPUT_DIRECTORY/sqlite-arm64.profdata"
"$LLVM_PROFDATA" merge -output="$MERGED_PROFILE" "${RAW_PROFILES[@]}"
configure_and_build pgo-use O2 OFF USE "$MERGED_PROFILE"
run_variant pgo-use

python3 "$SCRIPT_DIRECTORY/summarize_android_optimizations.py" \
  "$OUTPUT_DIRECTORY/o2.log" \
  "$OUTPUT_DIRECTORY/o3.log" \
  "$OUTPUT_DIRECTORY/thinlto.log" \
  "$OUTPUT_DIRECTORY/pgo-use.log" \
  | tee "$OUTPUT_DIRECTORY/summary.md"
