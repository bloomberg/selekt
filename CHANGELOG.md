Change Log
==========

## Version 0.19.0

### Dependencies

* Android Benchmark 1.2.0-alpha13.
* Android Gradle Plugin 8.0.1.
* Gradle 8.1.1.
* Java 17.
* Kotlin 1.8.21.
* SQLCipher 4.5.4.

## Version 0.18.2

### Dependencies

* Room 2.5.1.

## Version 0.18.1

### Dependencies

* Android Benchmark Gradle Plugin 1.1.1.
* Android Gradle Plugin 7.4.2.
* Dokka 1.8.10.
* Gradle 7.6.1.
* JUnit 5.9.2.
* Kotlin 1.8.20.
* Mockito 5.3.0.
* NDK 25.2.9519653.

## Version 0.18.0

### Dependencies

* OpenSSL 1.1.1t.

## Version 0.17.1

### Dependencies

* Android Gradle Plugin 7.4.1.
* Kotlin 1.8.10.

## Version 0.17.0

### Dependencies

* Android Build Tools 33.0.1.
* Android Gradle Plugin 7.3.1.
* Gradle 7.6.
* Kotlin 1.8.0.
* OpenSSL 1.1.1s.
* SQLCipher 4.5.3.

## Version 0.16.4

### Dependencies

* Android Gradle Plugin 7.2.2.
* Android SDK 33.
* CMake 3.22.1.
* Kotlin 1.7.10.
* Kotlin Coroutines 1.6.4.
* NDK 25.1.8937393.

## Version 0.16.3

### Dependencies

* Build Tools 33.0.0.
* Gradle 7.5.1.
* LiveData 2.5.0.
* NDK 25.0.8775105.
* OpenSSL 1.1.1q.
* SQLCipher 4.5.2.

## Version 0.16.2

### Fixes

* Use Junit5 for all tests including Android.

### Dependencies

* Android Benchmark Gradle Plugin 1.1.0.
* Android Gradle Plugin 7.2.1.
* Android SDK 32.
* Kotlin Coroutines 1.6.2.
* OpenSSL 1.1.1p.

## Version 0.16.1

### Fixes

* Friendlier Java APIs.
* Prefer `Files.createTempFile` when unpacking native libraries from the JAR in the JVM.

### Dependencies

* LiveData 2.4.1.
* Mockito 4.6.1.
* NDK 24.0.8215888.
* Robolectric 4.8.1.
* Room 2.4.2.

## Version 0.16.0

### Dependencies

* Android Gradle Plugin 7.2.0.
* OpenSSL 1.1.1o.

## Version 0.15.7

### Dependencies

* Detekt 1.20.0.
* Kotlin 1.6.21.
* Kotlin Coroutines 1.6.1.
* Ktlint 0.45.2
* Ktlint Gradle plugin 10.3.0.

## Version 0.15.6

### Dependencies

* Gradle 7.4.2.
* Kotlin 1.6.20.

## Version 0.15.5

### Fixes

* Correct our SQLCipher version property to read "4.5.1".

### Dependencies

* OpenSSL 1.1.1n.

## Version 0.15.4

### Dependencies

* Android Build Tools 32.0.0.
* Android Gradle Plugin 7.1.2.
* Gradle 7.4.1.
* Kotlin Coroutines 1.6.0
* Kover 0.5.0.
* Ktlint 0.44.0.
* SQLCipher 4.5.1.

## Version 0.15.3

### Dependencies

* AssertJ 3.22.0.
* Detekt 1.19.0.
* Dokka 1.6.10.
* Gradle 7.3.3.
* JMH 1.34.
* Ktlint 0.43.2.
* Ktlint Gradle plugin 10.2.1.
* Mockito 4.0.0.
* Robolectric 4.7.3.

## Version 0.15.2

### Dependencies

* Dokka 1.6.0.
* Gradle 7.3.2.
* Kotlin 1.6.10.
* NDK 23.1.7779620.
* OpenSSL 1.1.1m.
* Robolectric 4.7.2.

## Version 0.15.1

### Fixes

* Correct SQLCipher version name for publication.

### Dependencies

* Gradle 7.3.1.

## Version 0.15.0

### Dependencies

* Gradle 7.3.
* Kotlin 1.6.0.
* SQLCipher 4.5.0.

## Version 0.14.5

### Fixes

* Remove extraneous prepared statement reset when retrying for busy.

## Version 0.14.4

### Fixes

* Further reduce OpenSSL binary size with the no-filenames configuration option.

### Dependencies

* OpenSSL 1.1.1l.
* Kotlin 1.5.31.

## Version 0.14.3

### Fixes

* Move Experimental annotation into the dedicated annotations package, alongside the rest.

### Optimisations

* Reset and clear bindings for prepared statements in one JNI hop instead of two.

### Dependencies

* Android SDK 31.
* Kotlin Coroutines 1.5.1.

## Version 0.14.2

### Features

* Make `SQLiteDatabase.beginTransaction` et al public API but carrying a health warning.

### Optimisations

* Further reductions in the OpenSSL libcrypto.a binary size.

### Dependencies

* Android Gradle Plugin 7.0.2.
* Android Lint 30.0.2.
* Kotlin 1.5.30.

## Version 0.14.1

### Features

* Reduced OpenSSL binary size. 
* Publish Android benchmarks.
* Publish JMH benchmarks.
* Publish JVM integration tests.

### Dependencies

* Gradle 7.2.

## Version 0.14.0

### Fixes

* Remove all calls to `jcenter()` from our Gradle build scripts.

### Dependencies

* Android Gradle Plugin 7.0.0.
* Android Lint 30.0.0.
* Build Tools 31.0.0.
* Detekt 1.18.0.
* NDK LTS 23.0.
* OpenSSL 1.1.1k.
* Robolectric 4.6.1.

## Version 0.13.7

### Fixes

* Fix `SQLiteDatabase.yieldTransaction` so that the connection is temporarily returned to the pool even if the transaction is nested.
* Annotate `SQLiteDatabase.yieldTransaction` as throwing an `InterruptedException`, from the internal call to `Thread.sleep`.
* Forward the database reference to the received transaction block.

## Version 0.13.6

### Fixes

* Move the verification of the key size out of the JVM and closer to the native SQLite implementation. This makes it easier to swap out SQLite implementations.
* Introduce a Key convention, resolving the keying strategy outside of the JVM to make it easier to swap out SQLite implementations.
* The published Android sources jar is no longer empty.
* Publish a KDoc jar for the main Android subproject.

### Dependencies

* Dokka 1.5.0.
* Kotlin 1.5.21.

## Version 0.13.5

### Fixes

* Move SQLCipher for Android to its own Gradle subproject. This decouples Selekt from SQLCipher making it possible for other SQLite implementations to be swapped in.

## Version 0.13.4

### Fixes

* Close the `SingleObjectPool` factory under the pool's lock to make clear locally that all races with creating the connection while closing the pool really are avoided. The pool being closed means the database has been closed too and the sole connection should be idle. Any waiters trying to acquire this connection post-close will raise an exception, meaning there's no contention for the pool lock and no added delay clearing the key.

### Dependencies

* Android Gradle Plugin 4.2.2.
* Android NDK 21.4.7075529.
* Gradle 7.1.1.
* Licensee 1.1.0.
* Nexus Sonatype Gradle Plugin 1.1.0.

## Version 0.13.3

### Fixes

* Throw an `IllegalArgumentException` instead of an `IllegalStateException` when failing to add or remove a database from the registry.

### Dependencies

* Kotlin 1.5.20.
* Gradle 7.1.

## Version 0.13.2

### Optimisations

* Optimise trimming an SQL statement string just before resolving for its statement type.

## Version 0.13.1

### Fixes

* Attempt to destroy all idle connections in an eviction pass before ever throwing.

## Version 0.13.0

### Fixes

* Throw if we try to make a connection when the connection factory is closed.
* Scheduled eviction from the connection pool respects any cancellation of the eviction future.
* In `Mutex` only ever test the remaining available waiting time if the specified wait interval was non-negative to begin with.
* In `CommonObjectPool` decrement the internal connection count if making a connection should result in a thrown exception.
* In `CommonObjectPool` increment the internal connection count and then attempt to schedule for eviction, rather than the other way round.

### Features

* Support Java 11.

### Dependencies

* Android Gradle Plugin 4.2.1.
* Gradle 7.0.2.
* JaCoCo 0.8.7.
* Mockito 3.9.0.
* Mockito-Kotlin 3.2.0.

## Version 0.12.7

### Fixes

* Move the creation of the database connection keying SQL-statement string from the JVM into the JNI.

### Dependencies

* Android Gradle plugin 4.2.0.
* CMake 3.18.1.

## Version 0.12.6

### Fixes

* Fix convenience method for deleting database files, and extend its test coverage.
* Use the `ColumnType`-enum directly when converting to an Android Cursor column type. This increases code coverage by removing a branch we cannot test.
* Appeal instead to a Java-cast in Queries when casting an array, Kotlin's `as` results in a generated null-check branch which won't be encountered here and can't be tested. This increases code coverage.
* Reduce the number of logical branches in the pool implementations, simplifying these implementations and increasing their test coverage.

## Version 0.12.5

### Fixes

* Remove a redundant null-check around an atomic updater from `SharedCloseable`.
* Remove unused factory methods for activating, passivating and validating objects. Connections are encapsulated and entirely managed within a closed system.

### Optimisations

* Directly compare with the head of the waiter queue's address when deciding if the current thread is the next waiting thread, rather than via `Thread.equals`.

### Dependencies

* Gradle 7.0.
* Android Gradle Plugin 4.1.3.

## Version 0.12.4

### Fixes

* Add OpenSSL 1.1.1 dual license to the published Android artifact's pom.
* Provide Room example in integration instructions.
* Rename ci management element in pom.

## Version 0.12.3

### Fixes

* Add Zetetic LLC license to the published Android artifact's pom as well.

## Version 0.12.2

### Fixes

* Correct the common group co-ordinate for Selekt's remote dependencies.

## Version 0.12.1

Initial publication.
