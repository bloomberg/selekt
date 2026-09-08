# JDBC fuzzing

The JDBC module has Jazzer targets for its UTF-8 reader, JDBC URL parsing and
redaction, database-key encoding, SQLite/JDBC type conversion, and a bounded
native-backed JDBC state machine.

Run all targets locally with:

```bash
./gradlew :selekt-jdbc:jvmFuzz -Pselekt.fuzz.profile=smoke -x integrationTest
```

The available profiles are `smoke` (5 seconds per target), `release` (1 minute
per target), and `scheduled` (5 minutes per target). Individual targets can be
run with the `jvmFuzzUtf8Reader`, `jvmFuzzConnectionUrl`,
`jvmFuzzKeyEncoding`, `jvmFuzzTypeMapping`, or `jvmFuzzStateMachine` task in
the `selekt-jdbc` project.

Normal `:selekt-jdbc:test` execution runs deterministic seeds as fast
regression tests. Jazzer keeps generated corpora and crash reproducers under
`selekt-jdbc/build`, so fuzzing does not modify the source tree.
