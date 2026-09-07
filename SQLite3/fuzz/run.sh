#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 3 ]]; then
  echo "usage: run.sh ENGINE PROFILE BUILD_DIR" >&2
  exit 2
fi

engine=$1
profile=$2
build_dir=$3
fuzz_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
corpus_root="$build_dir/corpora"
artifact_root="$build_dir/artifacts"
python3 "$fuzz_dir/prepare_corpora.py" "$fuzz_dir" "$corpus_root"
mkdir -p "$artifact_root"

export ASAN_OPTIONS="abort_on_error=1:allocator_may_return_null=1:detect_leaks=1:symbolize=1"
export UBSAN_OPTIONS="halt_on_error=1:print_stacktrace=1"

targets=(
  "selekt_fuzz_sql:sql"
  "selekt_fuzz_vec1_scalar:scalar"
  "selekt_fuzz_vec1_shadow:shadow"
  "selekt_fuzz_database:database"
)

if [[ $engine == libfuzzer ]]; then
  case "$profile" in
    smoke) run_options=(-runs=2000) ;;
    release) run_options=(-max_total_time=60) ;;
    scheduled) run_options=(-max_total_time=600) ;;
    *) echo "Unsupported fuzz profile: $profile" >&2; exit 2 ;;
  esac
  for entry in "${targets[@]}"; do
    target=${entry%%:*}
    corpus=${entry#*:}
    options=(
      -artifact_prefix="$artifact_root/$target-"
      -max_len=1048576
      -rss_limit_mb=3072
      -timeout=10
    )
    if [[ $target == selekt_fuzz_sql ]]; then
      options+=(-dict="$fuzz_dir/sql.dict")
    fi
    "$build_dir/fuzz/$target" "${options[@]}" "${run_options[@]}" "$corpus_root/$corpus"
  done
elif [[ $engine == afl ]]; then
  case "$profile" in
    release) duration=60 ;;
    scheduled) duration=600 ;;
    *) echo "AFL++ runs only in release or scheduled profiles" >&2; exit 2 ;;
  esac
  export AFL_AUTORESUME=1
  export AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1
  export AFL_NO_UI=1
  export AFL_SKIP_CPUFREQ=1
  for entry in "${targets[@]}"; do
    target=${entry%%:*}
    corpus=${entry#*:}
    afl-fuzz \
      -i "$corpus_root/$corpus" \
      -o "$artifact_root/afl-$target" \
      -m none \
      -t 10000+ \
      -V "$duration" \
      -- "$build_dir/fuzz/$target" @@
  done
else
  echo "Unsupported fuzz engine: $engine" >&2
  exit 2
fi
