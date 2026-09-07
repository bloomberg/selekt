# Native fuzzing

Local Linux examples:

```bash
./gradlew :SQLite3:nativeFuzz
./gradlew :SQLite3:nativeFuzz \
  -Pselekt.fuzz.engine=afl \
  -Pselekt.fuzz.profile=release
```
