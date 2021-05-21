Change Log
==========

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
