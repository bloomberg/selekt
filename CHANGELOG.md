Change Log
==========

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
