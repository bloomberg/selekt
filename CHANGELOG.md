Change Log
==========

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
