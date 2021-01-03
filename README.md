# Selekt

[![Coverage Status](https://codecov.io/gh/bloomberg/selekt/branch/main/graph/badge.svg)](https://codecov.io/gh/bloomberg/selekt)

Selekt is a Kotlin and familiar Android wrapper over [SQLCipher](https://www.zetetic.net/sqlcipher/), an SQLite extension that provides 256-bit AES encryption of database files. Selekt realises the maximum concurrency offered by SQLite3.

## Menu

- [Rationale](#rationale)
- [Quick start](#quick-start)
- [Contributions](#contributions)
- [License](#license)
- [Code of Conduct](#code-of-conduct)
- [Security Vulnerability Reporting](#security-vulnerability-reporting)

## Rationale

The two most popular publicly available alternatives to Selekt are the Android SDK's own SQLite database, and [Android-SQLCipher](https://www.zetetic.net/sqlcipher/sqlcipher-for-android/). The Android SDK's SQLite database does not encrypt databases, instead relying on the OS's user security model to restrict access. Android-SQLCipher uses the SQLCipher library to encrypt databases, but because the derivation of every key is by design expensive it can't be allowed to make full use of the concurrency offered by SQLite3: Each database must have only one connection, connections cannot be ephemeral and must persist even if idling.

Selekt sits somewhere between the two: Selekt also uses SQLCipher to encrypt databases with AES-256, but uses SQLCipher in a mode that moves the responsibility for deriving keys to the caller. This compromise sacrifices some of the security guarantee offered by the default operating mode of SQLCipher, in return for allowing greater concurrency and efficient resource use while still retaining pretty good security.

## Quick Start

### Creating a database with an open helper

```kt
object MyOpenHelperCallback : ISQLiteOpenHelper.Callback {
    override fun onCreate(database: SQLiteDatabase) {
        database.exec("CREATE TABLE 'Foo' (bar INT)")
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

fun deriveKey(): ByteArray? = TODO("Optional key, must be exactly 32-bytes long.")

val databaseHelper = SQLiteOpenHelper(
    context = context.applicationContext,
    configuration = ISQLiteOpenHelper.Configuration(
        callback = MyOpenHelperCallback,
        key = deriveKey(),
        name = "sample"
    ),
    version = 3
)
```

## Contributions

We :heart: contributions.

Have you had a good experience with this project? Why not share some love and contribute code, or just let us know about any issues you had with it?

We welcome issue reports [here](../../issues); be sure to choose the proper issue template for your issue, so that we can be sure you're providing the necessary information.

Before sending a [Pull Request](../../pulls), please make sure you read our
[Contribution Guidelines](https://github.com/bloomberg/.github/blob/master/CONTRIBUTING.md).

## License

Please read the [LICENSE](LICENSE) file.

## Code of Conduct

This project has adopted a [Code of Conduct](https://github.com/bloomberg/.github/blob/master/CODE_OF_CONDUCT.md).
If you have any concerns about the Code, or behavior which you have experienced in the project, please
contact us at opensource@bloomberg.net.

## Security Vulnerability Reporting

If you believe you have identified a security vulnerability in this project, please send email to the project
team at opensource@bloomberg.net, detailing the suspected issue and any methods you've found to reproduce it.

Please do NOT open an issue in the GitHub repository, as we'd prefer to keep vulnerability reports private until
we've had an opportunity to review and address them.
