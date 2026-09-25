## Integration

Selekt JDBC supports Java 11 and later. For new projects, we recommend the current LTS release (Java 25 at the time of writing) which uses Selekt's FFM backend. Java 11–24 remain supported through the JNI backend for compatibility. Gradle uses published variant metadata to select the appropriate backend from the project's target JVM version, not the JVM running Gradle.

Maven does not consume Gradle variant metadata. Maven consumers therefore receive the unclassified Java 11 JNI backend on every supported Java version, including Java 25 or later. FFM backend selection is not currently supported for Maven consumers.

### Gradle

=== "Kotlin"
    ``` kotlin
    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(platform("com.bloomberg.selekt:selekt-bom:{selektVersion}"))
        implementation("com.bloomberg.selekt:selekt-jdbc")
    }
    ```

=== "Groovy"
    ``` groovy
    repositories {
        mavenCentral()
    }

    dependencies {
        implementation platform('com.bloomberg.selekt:selekt-bom:{selektVersion}')
        implementation 'com.bloomberg.selekt:selekt-jdbc'
    }
    ```

### Maven

``` xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.bloomberg.selekt</groupId>
            <artifactId>selekt-bom</artifactId>
            <version>{selektVersion}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>com.bloomberg.selekt</groupId>
        <artifactId>selekt-jdbc</artifactId>
    </dependency>
</dependencies>
```

Published artifacts include CycloneDX SBOMs. See [Software bill of materials](supply_chain.md) for publication,
runtime-inspection and generation details.

## Getting a connection

### Using a DataSource

=== "Kotlin"
    ``` kotlin
    val dataSource = SelektDataSource().apply {
        databasePath = "/path/to/database.db"
        journalMode = "WAL" // is the default
        busyTimeout = 2_500 // milliseconds is the default
        maxPoolSize = 4 // is the default, with 3 read connections
        cursorWindowSize = 1_024 // rows; the default
        cursorWindowByteSize = 2 * 1024 * 1024 // bytes; the default
        foreignKeys = true
    }

    dataSource.connection.use { connection ->
        // Use connection
    }
    ```

=== "Java"
    ``` java
    final SelektDataSource dataSource = new SelektDataSource();
    dataSource.setDatabasePath("/path/to/database.db");
    dataSource.setJournalMode("WAL"); // is the default
    dataSource.setBusyTimeout(2500); // milliseconds is the default
    dataSource.setMaxPoolSize(4); // is the default, with 3 read connections
    dataSource.setCursorWindowSize(1024); // rows; the default
    dataSource.setCursorWindowByteSize(2 * 1024 * 1024); // bytes; the default
    dataSource.setForeignKeys(true);

    try (Connection connection = dataSource.getConnection()) {
        // Use connection
    }
    ```

### Using DriverManager

=== "Kotlin"
    ``` kotlin
    val url = "jdbc:sqlite:/path/to/database.db"
    val connection = DriverManager.getConnection(url)
    ```

=== "Java"
    ``` java
    final String url = "jdbc:sqlite:/path/to/database.db";
    final Connection connection = DriverManager.getConnection(url);
    ```

Connection properties can be passed via a `Properties` object:

=== "Kotlin"
    ``` kotlin
    val properties = Properties().apply {
        setProperty("journalMode", "WAL")
        setProperty("busyTimeout", "2500")
        setProperty("poolSize", "4")
        setProperty("cursorWindowSize", "1024")
        setProperty("cursorWindowByteSize", "2097152")
        setProperty("foreignKeys", "true")
    }

    val connection = DriverManager.getConnection(
        "jdbc:sqlite:/path/to/database.db",
        properties
    )
    ```

=== "Java"
    ``` java
    final Properties properties = new Properties();
    properties.setProperty("journalMode", "WAL");
    properties.setProperty("busyTimeout", "2500");
    properties.setProperty("poolSize", "4");
    properties.setProperty("cursorWindowSize", "1024");
    properties.setProperty("cursorWindowByteSize", "2097152");
    properties.setProperty("foreignKeys", "true");

    final Connection connection = DriverManager.getConnection(
        "jdbc:sqlite:/path/to/database.db",
        properties
    );
    ```

Properties can also be inlined in the URL query string:

=== "Kotlin"
    ``` kotlin
    val connection = DriverManager.getConnection(
        "jdbc:sqlite:/path/to/database.db?journalMode=WAL&busyTimeout=2500&poolSize=4" +
            "&cursorWindowSize=1024&cursorWindowByteSize=2097152&foreignKeys=true"
    )
    ```

=== "Java"
    ``` java
    final Connection connection = DriverManager.getConnection(
        "jdbc:sqlite:/path/to/database.db?journalMode=WAL&busyTimeout=2500&poolSize=4" +
            "&cursorWindowSize=1024&cursorWindowByteSize=2097152&foreignKeys=true"
    );
    ```

## Encryption

Selekt uses SQLCipher for AES-256 encryption. Encryption is opt-in, databases are unencrypted by default. To enable encryption, provide a key that is exactly 32 bytes. Selekt treats these bytes as raw key material and does not apply PBKDF2 or another password-based key derivation function. Do not supply a human-readable password or passphrase; use cryptographically random bytes or the 32-byte output of a suitable key derivation function.

Represent arbitrary key bytes as a `CharArray` containing `0x` followed by exactly 64 hexadecimal digits. Selekt also accepts a non-prefixed `CharArray`, but encodes it as UTF-8; its encoded length, rather than its character count, must be exactly 32 bytes.

`SelektDriver` does not accept encryption keys. Encrypted connections must use `SelektDataSource.setEncryption` with an `EncryptionKeySource.Literal` backed by a caller-owned `CharArray`. `SelektDataSource` stores and later zeroes an internal copy; zero the caller-owned array after `setEncryption` returns.

### With a DataSource

=== "Kotlin"
    ``` kotlin
    private fun deriveHexEncodedKey(): CharArray = TODO(
        "Return 32 bytes from a cryptographically secure random-number generator or suitable " +
            "key derivation function, encoded as '0x' followed by 64 hexadecimal digits.")

    val dataSource = SelektDataSource().apply {
        databasePath = "/path/to/encrypted.db"
    }
    val key = deriveHexEncodedKey()
    try {
        dataSource.setEncryption(EncryptionKeySource.Literal(key))
    } finally {
        key.fill('\u0000')
    }
    ```

=== "Java"
    ``` java
    private char[] deriveHexEncodedKey() {
        // TODO Return 32 bytes from a cryptographically secure random-number generator or suitable
        // key derivation function, encoded as "0x" followed by 64 hexadecimal digits.
    }

    final SelektDataSource dataSource = new SelektDataSource();
    dataSource.setDatabasePath("/path/to/encrypted.db");
    final char[] key = deriveHexEncodedKey();
    try {
        dataSource.setEncryption(new EncryptionKeySource.Literal(key));
    } finally {
        java.util.Arrays.fill(key, '\0');
    }
    ```

## Interaction

### Querying with a PreparedStatement

=== "Kotlin"
    ``` kotlin
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            "SELECT id, name FROM users WHERE id = ?"
        ).use { statement ->
            statement.setInt(1, 42)
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    println(
                        "${resultSet.getInt("id")}: ${resultSet.getString("name")}"
                    )
                }
            }
        }
    }
    ```

=== "Java"
    ``` java
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(
             "SELECT id, name FROM users WHERE id = ?")) {
        statement.setInt(1, 42);
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                System.out.println(
                    resultSet.getInt("id") + ": " + resultSet.getString("name")
                );
            }
        }
    }
    ```

### Cursor memory and scrolling

Scrollable JVM cursors retain at most 1,024 rows and an estimated 2 MiB by default. Moving outside the retained window
re-runs the query to refill it, so request `ResultSet.TYPE_SCROLL_SENSITIVE`. Selekt does not advertise or accept
`TYPE_SCROLL_INSENSITIVE`; use a transaction when scrolling must observe a stable snapshot. A row whose estimated size
exceeds 2 MiB is rejected rather than copied into the cursor window.

Auto-commit `TYPE_FORWARD_ONLY` result sets—the JDBC default—stream rows and do not use these window limits. Selekt may
materialise a forward-only query inside a manual read-only transaction to avoid pinning its SQLite snapshot after the
query call.

### Inserting data

=== "Kotlin"
    ``` kotlin
    dataSource.connection.use { connection ->
        connection.prepareStatement(
            "INSERT INTO users (id, name) VALUES (?, ?)"
        ).use { statement ->
            statement.setInt(1, 1)
            statement.setString(2, "Alice")
            statement.executeUpdate()
        }
    }
    ```

=== "Java"
    ``` java
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(
             "INSERT INTO users (id, name) VALUES (?, ?)")) {
        statement.setInt(1, 1);
        statement.setString(2, "Alice");
        statement.executeUpdate();
    }
    ```

### Batch inserts

=== "Kotlin"
    ``` kotlin
    dataSource.connection.use { connection ->
        connection.autoCommit = false
        try {
            connection.prepareStatement(
                "INSERT INTO users (id, name) VALUES (?, ?)"
            ).use { statement ->
                for (i in 1..1000) {
                    statement.setInt(1, i)
                    statement.setString(2, "User $i")
                    statement.addBatch()
                }
                statement.executeBatch()
            }
            connection.commit()
        } catch (e: SQLException) {
            connection.rollback()
            throw e
        }
    }
    ```

=== "Java"
    ``` java
    try (Connection connection = dataSource.getConnection()) {
        connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO users (id, name) VALUES (?, ?)"
        )) {
            for (int i = 1; i <= 1000; i++) {
                statement.setInt(1, i);
                statement.setString(2, "User " + i);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
        } catch (final SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    ```

`executeBatch()` returns a caller-owned update-count array that remains independent of later executions. Applications that deliberately want to avoid this copy can unwrap `SelektPreparedStatement` and request a shared array:

``` kotlin
val updateCounts = statement.unwrap<SelektPreparedStatement>().executeBatchShared()
```

The shared array is driver-owned and must be treated as read-only. A later batch execution or closing and pooling the statement may reuse it.

### Transactions

=== "Kotlin"
    ``` kotlin
    dataSource.connection.use { connection ->
        connection.autoCommit = false
        try {
            connection.prepareStatement(
                "UPDATE accounts SET balance = balance - ? WHERE id = ?"
            ).use { statement ->
                statement.setDouble(1, 100.0)
                statement.setInt(2, 1)
                statement.executeUpdate()
            }
            connection.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE id = ?"
            ).use { statement ->
                statement.setDouble(1, 100.0)
                statement.setInt(2, 2)
                statement.executeUpdate()
            }
            connection.commit()
        } catch (e: SQLException) {
            connection.rollback()
            throw e
        }
    }
    ```

=== "Java"
    ``` java
    try (Connection connection = dataSource.getConnection()) {
        connection.setAutoCommit(false);
        try {
            try (final PreparedStatement statement = connection.prepareStatement(
                "UPDATE accounts SET balance = balance - ? WHERE id = ?"
            )) {
                statement.setDouble(1, 100.0);
                statement.setInt(2, 1);
                statement.executeUpdate();
            }
            try (final PreparedStatement statement = connection.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE id = ?"
            )) {
                statement.setDouble(1, 100.0);
                statement.setInt(2, 2);
                statement.executeUpdate();
            }
            connection.commit();
        } catch (final SQLException e) {
            connection.rollback();
            throw e;
        }
    }
    ```

## Connection properties

| Property        | Type    | Default | Description                                                                   |
|-----------------|---------|---------|-------------------------------------------------------------------------------|
| `journalMode`   | String  | `WAL`   | SQLite journal mode (`DELETE`, `TRUNCATE`, `PERSIST`, `MEMORY`, `WAL`, `OFF`) |
| `busyTimeout`   | int     | `2500`  | SQLite busy timeout in milliseconds                                           |
| `poolSize`      | int     | `4`     | Maximum connection pool size                                                  |
| `foreignKeys`   | boolean | `true`  | Enable foreign key constraints                                                |

## Closing the DataSource

=== "Kotlin"
    ``` kotlin
    dataSource.close()
    ```

=== "Java"
    ``` java
    dataSource.close();
    ```

Calling `close()` releases all pooled connections and zeroes any encryption key material. The method is idempotent.
