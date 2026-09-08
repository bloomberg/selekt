## Integration

Selekt Android requires Java 17.

### Gradle

=== "Kotlin"
    ``` kotlin
    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(platform("com.bloomberg.selekt:selekt-bom:{selektVersion}"))
        implementation("com.bloomberg.selekt:selekt-android")
        runtimeOnly("com.bloomberg.selekt:selekt-android-sqlcipher")
    }
    ```

=== "Groovy"
    ``` groovy
    repositories {
        mavenCentral()
    }

    dependencies {
        implementation platform('com.bloomberg.selekt:selekt-bom:{selektVersion}')
        implementation 'com.bloomberg.selekt:selekt-android'
        runtimeOnly 'com.bloomberg.selekt:selekt-android-sqlcipher'
    }
    ```

## Getting a database

### Using Room

#### Room 2.8 and later (Recommended)

Use `SelektSQLiteDriver` with Room 2.8 and later:

The key must contain exactly 32 bytes and must not be all zero. The array remains yours: the driver does not modify it and
copies it into protected native memory before creation returns. Clear your array immediately, including when driver
creation fails. Room does not close the driver; retain it for the database's lifetime, then close the database before the
driver. Closing the driver releases its native key.

=== "Kotlin"
    ``` kotlin
    import com.bloomberg.selekt.android.room.createSelektSQLiteDriver

    private fun deriveKey(): ByteArray? = TODO(
        "Optional key, must be exactly 32-bytes long.")

    val key = deriveKey()
    val driver = try {
        createSelektSQLiteDriver(SQLiteJournalMode.WAL, key)
    } finally {
        key?.fill(0)
    }

    val database = Room.databaseBuilder(context, MyAppDatabase::class.java, "app")
        .setDriver(driver)
        .build()

    // When shutting down, close the database before its driver.
    database.close()
    driver.close()
    ```

=== "Java"
    ``` java
    import com.bloomberg.selekt.android.room.SelektSQLiteDriver;
    import com.bloomberg.selekt.android.room.SelektSQLiteDriverKt;
    import java.util.Arrays;

    private byte[] deriveKey() {
        // TODO Optional key, must be exactly 32-bytes long.
    }

    final byte[] key = deriveKey();
    final SelektSQLiteDriver driver;
    try {
        driver = SelektSQLiteDriverKt.createSelektSQLiteDriver(
            SQLiteJournalMode.WAL,
            key);
    } finally {
        if (key != null) {
            Arrays.fill(key, (byte) 0);
        }
    }

    final RoomDatabase database = Room.databaseBuilder(
        context, MyAppDatabase.class, "app"
    ).setDriver(driver)
        .build();

    // When shutting down, close the database before its driver.
    database.close();
    driver.close();
    ```

#### Room 2.7 and earlier (Legacy)

For older Room versions, use `SupportSQLiteOpenHelperFactory`:

=== "Kotlin"
    ``` kotlin
    private fun deriveKey(): ByteArray? = TODO(
        "Optional key, must be exactly 32-bytes long.")

    private val factory = run {
        val key = deriveKey()
        try {
            SupportSQLiteOpenHelperFactory(SQLiteJournalMode.WAL, key)
        } finally {
            key?.fill(0)
        }
    }

    val database = try {
        Room.databaseBuilder(context, MyAppDatabase::class.java, "app")
            .openHelperFactory(factory)
            .build()
    } finally {
        // Room has created its helper, which owns a separate key copy.
        factory.close()
    }
    ```

=== "Java"
    ``` java
    private byte[] deriveKey() {
        // TODO Optional key, must be exactly 32-bytes long.
    }

    final byte[] key = deriveKey();
    final SupportSQLiteOpenHelperFactory factory;
    try {
        factory = new SupportSQLiteOpenHelperFactory(SQLiteJournalMode.WAL, key);
    } finally {
        if (key != null) {
            Arrays.fill(key, (byte) 0);
        }
    }

    final RoomDatabase database;
    try {
        database = Room.databaseBuilder(context, MyAppDatabase.class, "app")
            .openHelperFactory(factory)
            .build();
    } finally {
        // Room has created its helper, which owns a separate key copy.
        factory.close();
    }
    ```

The factory copies the key during construction and never modifies the caller's array. Closing it zeroes its private copy
and prevents creation of further helpers; helpers already created by Room remain usable.

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
            new MyOpenHelperCallback(),
            deriveKey(),
            "sample"
        )
    );
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

## Native ABI compatibility

Selekt's packaged native libraries target the following Android ABIs:

* armeabi-v7a
* arm64-v8a
* x86
* x86_64

If your app only supports a subset of these architectures, configure ABI filtering to reduce APK size and avoid installs on unsupported devices.

### Restricting native libraries

To support only 64-bit ABIs:

=== "Kotlin"
    ``` kotlin
    android {
        ndk {
            abiFilters.addAll(arrayOf("arm64-v8a", "x86_64"))
        }
    }
    ```

=== "Groovy"
    ``` groovy
    android {
        ndk {
            abiFilters 'arm64-v8a', 'x86_64'
        }
    }
    ```

### Excluding native libraries

Alternatively, exclude ABIs while packaging. For example, for an APK that only supports ARM architectures:

=== "Kotlin"
    ``` kotlin
    android {
        packagingOptions {
            exclude("/lib/x86/*")
            exclude("/lib/x86_64/*")
        }
    }
    ```

=== "Groovy"
    ``` groovy
    android {
        packagingOptions {
            exclude '/lib/x86/*'
            exclude '/lib/x86_64/*'
        }
    }
    ```
