#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "usage: build.sh ENGINE BUILD_DIR" >&2
  exit 2
fi

engine=$1
build_dir=$2
if [[ $(uname -s) != Linux ]]; then
  echo "Native fuzzing is supported only on Linux" >&2
  exit 2
fi
case "$(uname -m)" in
  x86_64) target_abi=linux-amd64 ;;
  aarch64|arm64) target_abi=linux-aarch64 ;;
  *) echo "Unsupported CI fuzzing architecture: $(uname -m)" >&2; exit 2 ;;
esac

repo_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
cd "$repo_dir"
if [[ ${SELEKT_FUZZ_SKIP_GRADLE:-0} != 1 ]]; then
  ./gradlew :OpenSSL:assembleHost :SQLite3:amalgamate
fi

if [[ $engine == libfuzzer ]]; then
  c_compiler=clang
  cxx_compiler=clang++
elif [[ $engine == afl ]]; then
  c_compiler=afl-clang-fast
  cxx_compiler=afl-clang-fast++
else
  echo "Unsupported fuzz engine: $engine" >&2
  exit 2
fi

cmake -S SQLite3 -B "$build_dir" -G Ninja \
  -DCMAKE_BUILD_TYPE=RelWithDebInfo \
  -DCMAKE_C_COMPILER="$c_compiler" \
  -DCMAKE_CXX_COMPILER="$cxx_compiler" \
  -DSLKT_TARGET_ABI="$target_abi" \
  -DSELEKT_BUILD_FUZZERS=ON \
  -DSELEKT_ENABLE_VEC1=ON \
  -DSELEKT_VEC1_ENABLE_X86_AVX2=OFF \
  -DSELEKT_FUZZ_ENGINE="$engine"
cmake --build "$build_dir" --target selekt_fuzzers --parallel "$(nproc)"
