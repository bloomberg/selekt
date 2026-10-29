# Native fuzzing

The native campaign combines generic SQL/database targets with focused vec1 targets:

- `selekt_fuzz_vec1_scalar` exercises JSON, configuration and scalar SQL functions;
- `selekt_fuzz_vec1_shadow` mutates model, index, base and metadata storage;
- `selekt_fuzz_vec1_lifecycle` drives transactions, savepoints, rebuilds, two connections and reconnects;
- `selekt_fuzz_vec1_metadata` compares typed predicates across SQLite, no-index vec1 and flat vec1;
- `selekt_fuzz_vec1_training` uses reduced fuzz-build limits and requires every returned model to be installable; and
- `selekt_fuzz_vec1_distance` compares dispatched scalar/AVX2/NEON arithmetic with a long-double reference.

Fuzz builds reduce training-only limits so chunk and model boundaries are reachable in short campaigns. Production
limits and formats are unchanged. On x86-64 the campaign builds both scalar and AVX2/FMA vec1 implementations and
uses runtime dispatch. AArch64 builds exercise the NEON implementation.

Local Linux examples:

```bash
./gradlew :SQLite3:nativeFuzz
./gradlew :SQLite3:nativeFuzz \
  -Pselekt.fuzz.engine=afl \
  -Pselekt.fuzz.profile=release
```

The `smoke`, `release` and `scheduled` libFuzzer profiles run every target for 2,000 iterations, 60 seconds and ten
minutes respectively. The corpus generator commits semantic seeds for transaction/rebuild schedules, embedded NUL,
numeric boundaries, degenerate training data, padded codebooks, SIMD lane boundaries and scalar tails.

ASan and UBSan are enabled for every target, including both x86 vec1 object variants. TSan, MSan and Android 32-bit
campaigns require separate toolchains/runners and are intentionally not implied by these local targets.
