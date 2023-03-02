# Selekt

[![Apache 2.0](https://img.shields.io/badge/license-Apache%202-blue.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.bloomberg/selekt-android.svg)](https://search.maven.org/artifact/com.bloomberg/selekt-android)
[![KDoc](https://img.shields.io/static/v1?label=docs&message=KDoc&color=1AA2D4)](https://bloomberg.github.io/selekt/kdoc/api/selekt-android/index.html)

[![OpenSSF
Scorecard](https://api.securityscorecards.dev/projects/github.com/bloomberg/selekt/badge)](https://api.securityscorecards.dev/projects/github.com/bloomberg/selekt)

Selekt is a Kotlin and familiar Android SQLite database library that by default wraps the community edition of [SQLCipher](https://www.zetetic.net/sqlcipher/open-source/), an SQLite extension that provides 256-bit AES encryption of database files. Selekt realises the maximum concurrency offered by SQLite3: When enabled for WAL-journal mode, "readers do not block writers and a writer does not block readers. Reading and writing can proceed concurrently."

The Selekt project is used to securely and efficiently store data in the [Bloomberg Professional](https://play.google.com/store/apps/details?id=com.bloomberg.android.anywhere) application for Android.

## Menu

- [Rationale](#rationale)
- [Quick start](#quick-start)
- [Contributions](#contributions)
- [Licenses](#licenses)
- [Code of Conduct](#code-of-conduct)
- [Security Vulnerability Reporting](#security-vulnerability-reporting)

## Rationale

The two most popular publicly available alternatives to Selekt are the Android SDK's own SQLite database, and [SQLCipher for Android](https://www.zetetic.net/sqlcipher/sqlcipher-for-android/). The Android SDK's SQLite database does not encrypt databases, instead relying on the OS's user security model to restrict access. SQLCipher for Android uses the SQLCipher library to encrypt databases, but because the derivation of every key is by design expensive it can't also be allowed to make full use of the concurrency offered by SQLite3: Each database must have only one connection, connections cannot be ephemeral and must persist even if idling.

Selekt sits somewhere between the two: Selekt also uses SQLCipher to encrypt databases with AES-256, but uses SQLCipher in a mode that moves the responsibility for deriving keys to the caller. This sacrifices some of the security guarantee offered by the default operating mode of SQLCipher, in return for allowing greater concurrency and efficient resource use by pooling connections while still retaining pretty good security.

## Quick Start

Please refer to the [main documentation](https://bloomberg.github.io/selekt/getting_started/).

## Contributions

We :heart: contributions.

Have you had a good experience with this project? Why not share some love and contribute code, or just let us know about any issues you had with it?

We welcome issue reports [here](../../issues); be sure to choose the proper issue template for your issue, so that we can be sure you're providing the necessary information.

Before sending a [Pull Request](../../pulls), please make sure you read our
[Contribution Guidelines](https://github.com/bloomberg/.github/blob/master/CONTRIBUTING.md).

## Licenses

Please read the [LICENSE](LICENSE), [OPENSSL_LICENSE](OPENSSL_LICENSE) and [SQLCIPHER_LICENSE](SQLCIPHER_LICENSE) files.

## Code of Conduct

This project has adopted a [Code of Conduct](https://github.com/bloomberg/.github/blob/master/CODE_OF_CONDUCT.md).
If you have any concerns about the Code, or behavior which you have experienced in the project, please
contact us at opensource@bloomberg.net.

## Security Vulnerability Reporting

If you believe you have identified a security vulnerability in this project, please send an email to the project
team at opensource@bloomberg.net, detailing the suspected issue and any methods you've found to reproduce it.

Please do NOT open an issue in the GitHub repository, as we'd prefer to keep vulnerability reports private until
we've had an opportunity to review and address them.
