## Integration

Selekt requires **Java 25** or later.

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

## Getting a connection

### Using a DataSource

=== "Kotlin"
    ``` kotlin
    val dataSource = SelektDataSource().apply {
        databasePath = "/path/to/database.db"
        journalMode = "WAL" // is the default journal mode
        busyTimeout = 2_500 // milliseconds is the default busy timeout
        maxPoolSize = 4 // is the default, 3 read-only connections
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
    dataSource.setJournalMode("WAL"); // is the default journal mode
    dataSource.setBusyTimeout(2500); // milliseconds is the default busy timeout
    dataSource.setMaxPoolSize(4); // is the default, 3 read-only connections
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

Connection properties can be passed via the URL query string or a `Properties` object:

=== "Kotlin"
    ``` kotlin
    val url = "jdbc:sqlite:/path/to/database.db" +
        "?journalMode=WAL&busyTimeout=2500&poolSize=4&foreignKeys=true"
    val connection = DriverManager.getConnection(url)
    ```

=== "Java"
    ``` java
    final Properties properties = new Properties();
    properties.setProperty("journalMode", "WAL");
    properties.setProperty("busyTimeout", "2500");
    properties.setProperty("poolSize", "4");
    properties.setProperty("foreignKeys", "true");

    final Connection connection = DriverManager.getConnection(
        "jdbc:sqlite:/path/to/database.db",
        properties
    );
    ```

## Encryption

Selekt uses SQLCipher for AES-256 encryption. Encryption is **opt-in**, databases are unencrypted by default. To enable encryption, provide a key that is exactly **32 bytes**.

### With a DataSource

=== "Kotlin"
    ``` kotlin
    private fun deriveKey(): CharArray = TODO(
        "Derive a 32-byte encryption key.")

    val dataSource = SelektDataSource().apply {
        databasePath = "/path/to/encrypted.db"
        setEncryption(EncryptionKeySource.Literal(deriveKey()))
    }
    ```

=== "Java"
    ``` java
    private char[] deriveKey() {
        // TODO Derive a 32-byte encryption key.
    }

    final SelektDataSource dataSource = new SelektDataSource();
    dataSource.setDatabasePath("/path/to/encrypted.db");
    dataSource.setEncryption(
        new EncryptionKeySource.Literal(deriveKey()));
    ```

### With a hex key

=== "Kotlin"
    ``` kotlin
    val key = "0x0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF"
    dataSource.setEncryption(
        EncryptionKeySource.Literal(key.toCharArray())
    )
    ```

=== "Java"
    ``` java
    final String key =
        "0x0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";
    dataSource.setEncryption(
        new EncryptionKeySource.Literal(key.toCharArray()));
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
| `key`           | String  | `null`  | Encryption key (hex string, via `DriverManager` only)                         |

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
