#!/usr/bin/env python3

import json
import pathlib
import sys

PREFIXES = ("SELEKT_NATIVE_BENCHMARK ", "SELEKT_ANDROID_BENCHMARK ")


def read_results(path: pathlib.Path) -> dict[tuple[str, str, int], dict[str, object]]:
    results: dict[tuple[str, str, int], dict[str, object]] = {}
    for line in path.read_text().splitlines():
        prefix = next((candidate for candidate in PREFIXES if line.startswith(candidate)), None)
        if prefix is None:
            continue
        result = json.loads(line[len(prefix) :])
        key = (str(result["component"]), str(result["benchmark"]), int(result["dimension"]))
        results[key] = result
    if not results:
        raise ValueError(f"No benchmark results found in {path}")
    return results


def label(path: pathlib.Path) -> str:
    return path.stem


def main(arguments: list[str]) -> int:
    if len(arguments) < 2:
        print("usage: summarize_android_optimizations.py BASELINE.log VARIANT.log...", file=sys.stderr)
        return 2
    paths = [pathlib.Path(argument) for argument in arguments]
    variants = {label(path): read_results(path) for path in paths}
    baseline_name = label(paths[0])
    baseline = variants[baseline_name]
    expected = set(baseline)
    for name, results in variants.items():
        if set(results) != expected:
            missing = expected - set(results)
            extra = set(results) - expected
            raise ValueError(f"{name} benchmark keys differ; missing={sorted(missing)}, extra={sorted(extra)}")

    names = list(variants)
    print("# Android ARM64 native optimisation benchmark")
    print()
    print("Lower latency is better. Changes are relative to the O2 baseline.")
    print()
    print("| Component | Benchmark | Dimension | " + " | ".join(names) + " |")
    print("|---|---|---:|" + "---:|" * len(names))
    for component, benchmark, dimension in sorted(expected):
        baseline_ns = float(baseline[(component, benchmark, dimension)]["nanosecondsPerOperation"])
        cells: list[str] = []
        for name in names:
            value = float(variants[name][(component, benchmark, dimension)]["nanosecondsPerOperation"])
            if name == baseline_name:
                cells.append(f"{value:,.1f} ns")
            else:
                change = (value / baseline_ns - 1.0) * 100.0
                cells.append(f"{value:,.1f} ns ({change:+.1f}%)")
        print(
            f"| {component} | {benchmark} | {dimension or '—'} | "
            + " | ".join(cells)
            + " |"
        )
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
