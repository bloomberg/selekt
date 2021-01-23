## Integration

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

    private fun deriveKey(): ByteArray? = TODO("Optional key.")

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
        // TODO Optional key.
    }

    final SQLiteOpenHelper databaseHelper = new SQLiteOpenHelper(
        context.applicationContext,
        3,
        new ISQLiteOpenHelper.Configuration(
            MyOpenHelperCallback,
            deriveKey(),
            "sample"
        )
    )
    ```

### Using Room

=== "Kotlin"
    ``` kotlin
    import com.bloomberg.selekt.android.support.buildRoomDatabase

    private fun deriveKey(): ByteArray? = TODO("Optional key.")

    buildRoomDatabase(
        applicationContext,
        MyAppDatabase::class.java,
        "sample",
        SQLiteJournalMode.WAL,
        deriveKey()
    )
    ```

=== "Java"
    ``` java
    import com.bloomberg.selekt.android.support.buildRoomDatabase;

    private byte[] deriveKey() {
        // TODO Optional key.
    }

    buildRoomDatabase(
        applicationContext,
        MyAppDatabase.class,
        "sample.db",
        SQLiteJournalMode.WAL,
        deriveKey()
    )
    ```

### Directly

=== "Kotlin"
    ``` kotlin
    private fun deriveKey(): ByteArray? = TODO("Optional key.")

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
        // TODO Optional key.
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
        insertWithOnConflict(
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
    database.insertWithOnConflict(
        "Foo",
        values,
        ConflictAlgorithm.REPLACE
    );
    try (Cursor cursor = query(false, "Foo", arrayOf("bar"), null, null)) {
        cursor.moveToFirst();
        System.out.println(it.getInt(0));
    }
    ```
