## Integration

### Gradle

=== "Kotlin"
    ``` kotlin
    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("com.bloomberg:selekt-android:<version>")
    }
    ```

=== "Groovy"
    ``` groovy
    repositories {
        mavenCentral()
    }

    dependencies {
        implementation 'com.bloomberg:selekt-android:<version>'
    }
    ```

## Getting a database

### Using an open helper

=== "Kotlin"
    ``` kotlin
    object MyOpenHelperCallback : ISQLiteOpenHelper.Callback {
        override fun onCreate(database: SQLiteDatabase) {
            database.exec("CREATE TABLE 'Foo' (bar INT)")
        }

        override fun onUpgrade(
            database: SQLiteDatabase,
            oldVersion: Int,
            newVersion: Int
        ) {
            TODO("Migrate database.")
        }
    }

    private fun deriveKey(): ByteArray? = TODO(
        "Optional key, must be exactly 32-bytes long.")

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

=== "Java"
    ``` java
    class MyOpenHelperCallback implements ISQLiteOpenHelper.Callback {
        @Override
        public void onCreate(final SQLiteDatabase database) {
            database.exec("CREATE TABLE 'Foo' (bar INT)")
        }

        @Override
        public void onUpgrade(
            final SQLiteDatabase database,
            final int oldVersion,
            final int newVersion
        ) {
            // TODO Migrate database.
        }
    }

    private byte[] deriveKey() {
        // TODO Optional key, must be exactly 32-bytes long.
    }

    final SQLiteOpenHelper databaseHelper = new SQLiteOpenHelper(
        context.applicationContext,
        3,
        new ISQLiteOpenHelper.Configuration(
            MyOpenHelperCallback,
            deriveKey(),
            "sample"
        )
    );
    ```

### Using Room

=== "Kotlin"
    ``` kotlin
    private fun deriveKey(): ByteArray? = TODO(
        "Optional key, must be exactly 32-bytes long.")

    provate val factory = createSupportSQLiteOpenHelperFactory(
        SQLiteJournalMode.WAL,
        deriveKey()
    )

    val database = Room.databaseBuilder(context, MyAppDatabase::class.java, "app")
        .openHelperFactory(factory)
        .build()
    ```

=== "Java"
    ``` java
    private byte[] deriveKey() {
        // TODO Optional key, must be exactly 32-bytes long.
    }

    provate SupportSQLiteOpenHelper.Factory factory =
        SupportSQLiteOpenHelperKt.createSupportSQLiteOpenHelperFactory(
            SQLiteJournalMode.WAL,
            deriveKey());

    final RoomDatabase database = Room.databaseBuilder(
        context, MyAppDatabase.class, "app"
    ).openHelperFactory(factory)
        .build();
    ```

### Directly

=== "Kotlin"
    ``` kotlin
    private fun deriveKey(): ByteArray? = TODO(
        "Optional key, must be exactly 32-bytes long.")

    SQLiteDatabase.openOrCreateDatabase(
        context.getDatabasePath("sample"),
        SQLiteJournalMode.WAL.databaseConfiguration,
        deriveKey()
    ).apply {
        exec("PRAGMA journal_mode=${SQLiteJournalMode.WAL}")
    }
    ```

=== "Java"
    ``` java
    private byte[] deriveKey() {
        // TODO Optional key, must be exactly 32-bytes long.
    }

    final SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(
        targetContext.getDatabasePath("sample"),
        SQLiteJournalMode.WAL.getDatabaseConfiguration(),
        deriveKey()
    );
    database.exec("PRAGMA journal_mode=WAL");
    ```

## Interaction

### Querying the database

=== "Kotlin"
    ``` kotlin
    databaseHelper.writableDatabase.run {
        insert(
            "Foo",
            ContentValues().apply { put("bar", 42) },
            ConflictAlgorithm.REPLACE
        )
        query(false, "Foo", arrayOf("bar"), null, null).use {
            it.moveToFirst()
            println(it.getInt(0))
        }
    }
    ```

=== "Java"
    ``` java
    final SQLiteDatabase database = databaseHelper.getWritableDatabase();
    final ContentValues values = new ContentValues();
    values.put("bar", 42);
    database.insert(
        "Foo",
        values,
        ConflictAlgorithm.REPLACE
    );
    try (Cursor cursor = query(false, "Foo", arrayOf("bar"), null, null)) {
        cursor.moveToFirst();
        System.out.println(it.getInt(0));
    }
    ```
