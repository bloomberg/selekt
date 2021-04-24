Change Log
==========

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
