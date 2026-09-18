/*
 * Copyright 2020 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt.android

import android.content.Context
import android.database.sqlite.SQLiteException
import com.bloomberg.selekt.DatabaseConfiguration
import com.bloomberg.selekt.DatabaseKey
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.SQLiteTraceEventMode
import java.io.Closeable
import java.io.File
import javax.annotation.concurrent.GuardedBy
import javax.annotation.concurrent.NotThreadSafe

/**
 * The [SQLiteOpenHelper] class contains a useful set of APIs for managing your database. When you use this class to
 * obtain references to your database, the system performs the potentially long-running operations of creating and updating
 * the database lazily and only when needed, not during application startup. All you need to do is call
 * [writableDatabase()].
 *
 * The optional constructor key remains owned by the caller. This helper copies it during construction and does not retain
 * the supplied array. The caller should clear the array immediately after construction, including when construction throws.
 *
 * @since 0.1.0
 */
class SQLiteOpenHelper internal constructor(
    private val file: File,
    private val configuration: ISQLiteOpenHelper.Configuration,
    databaseConfiguration: DatabaseConfiguration,
    private val openParams: SQLiteOpenParams,
    version: Int,
    key: ByteArray?
) : ISQLiteOpenHelper {
    constructor(
        context: Context,
        configuration: ISQLiteOpenHelper.Configuration,
        version: Int,
        openParams: SQLiteOpenParams = SQLiteOpenParams(),
        key: ByteArray? = null
    ) : this(
        file = context.getDatabasePath(configuration.name),
        openParams = openParams,
        configuration = configuration,
        databaseConfiguration = openParams.journalMode.databaseConfiguration.copy(trace = openParams.trace),
        key = key,
        version = version
    )

    /**
     * Creates a helper with an explicit database configuration.
     */
    constructor(
        context: Context,
        configuration: ISQLiteOpenHelper.Configuration,
        version: Int,
        databaseConfiguration: DatabaseConfiguration,
        openParams: SQLiteOpenParams = SQLiteOpenParams(),
        key: ByteArray? = null
    ) : this(
        file = context.getDatabasePath(configuration.name),
        openParams = openParams,
        configuration = configuration,
        databaseConfiguration = databaseConfiguration.copy(trace = openParams.trace),
        key = key,
        version = version
    )

    init {
        require(version > 0) { "Version must be at least 1." }
    }

    private var databaseKey: DatabaseKey? = key?.let {
        require(it.any { byte -> byte != 0.toByte() }) {
            "Encryption keys must not consist entirely of zero bytes."
        }
        DatabaseKey.of(SQLite, it)
    }
    private val hasKey = key != null

    private val lifecycleLock = Any()
    @GuardedBy("lifecycleLock")
    private var keyDestroyed = false
    @GuardedBy("lifecycleLock")
    private var initializationInProgress = false
    @GuardedBy("lifecycleLock")
    private var closeRequestedDuringInitialization = false

    private val lazyDatabase = lazy(lifecycleLock) {
        check(!hasKey || !keyDestroyed) {
            "The encryption key has already been destroyed and cannot be reused to open this database."
        }
        initializationInProgress = true
        closeRequestedDuringInitialization = false
        var database: SQLiteDatabase? = null
        try {
            val openedDatabase = databaseKey?.let {
                SQLiteDatabase.openOrCreateDatabaseWithKey(file, databaseConfiguration, it)
            } ?: SQLiteDatabase.openOrCreateDatabase(file, databaseConfiguration, null)
            openedDatabase.also {
                database = it
                it.setPageSizeExponent(openParams.pageSizeExponent)
                it.setJournalMode(openParams.journalMode)
                configuration.callback.onConfigure(it)
                checkCloseNotRequested()
                val currentVersion = it.version
                if (currentVersion != version) {
                    it.transact {
                        when {
                            currentVersion == 0 -> configuration.callback.onCreate(it)
                            currentVersion > version -> configuration.callback.onDowngrade(
                                it,
                                oldVersion = currentVersion,
                                newVersion = version
                            )
                            else -> configuration.callback.onUpgrade(
                                it,
                                oldVersion = currentVersion,
                                newVersion = version
                            )
                        }
                        checkCloseNotRequested()
                    }
                    it.version = version
                }
                configuration.callback.onOpen(it)
                checkCloseNotRequested()
                destroyHelperKey()
            }
        } catch (failure: Throwable) {
            try {
                database?.close()
            } catch (cleanupFailure: Throwable) {
                if (cleanupFailure !== failure) {
                    failure.addSuppressed(cleanupFailure)
                }
            }
            throw failure
        } finally {
            initializationInProgress = false
        }
    }

    private fun checkCloseNotRequested() = check(!closeRequestedDuringInitialization) {
        "The database helper was closed while the database was being initialized."
    }

    @GuardedBy("lifecycleLock")
    private fun destroyHelperKey() {
        databaseKey?.close()
        databaseKey = null
        keyDestroyed = true
    }

    /**
     * Create and/or open a database that will be used for reading and writing. The first time this is called, the database
     * will be opened and [onCreate(SQLiteDatabase)] and [onUpgrade(SQLiteDatabase, int, int)] or
     * [onDowngrade(SQLiteDatabase)] can be called and each inside a transaction. If the transaction succeeds
     * [onOpen(SQLiteDatabase)] is called.
     *
     * Once opened successfully, the database is cached, so you can call this method every time you need to write to the
     * database. (Make sure to call close() when you no longer need the database.) Errors such as bad permissions or a full
     * disk may cause this method to fail, but future attempts may succeed if the problem is fixed.
     */
    override val writableDatabase: SQLiteDatabase by lazyDatabase

    override val databaseName = configuration.name

    override fun close() = synchronized(lifecycleLock) {
        if (initializationInProgress) {
            closeRequestedDuringInitialization = true
        }
        try {
            if (lazyDatabase.isInitialized()) {
                writableDatabase.close()
            }
        } finally {
            destroyHelperKey()
        }
    }
}

/**
 * Wrapper for configuration parameters that are used for opening a [SQLiteDatabase].
 */
data class SQLiteOpenParams(
    val journalMode: SQLiteJournalMode = SQLiteJournalMode.WAL,
    val pageSizeExponent: Int = 12,
    val trace: SQLiteTraceEventMode? = null
) {
    init {
        require(pageSizeExponent in LOWEST_PAGE_SIZE_EXPONENT..HIGHEST_PAGE_SIZE_EXPONENT) {
            "The page size must be a power of two between 512 and 65536 inclusive."
        }
    }

    companion object {
        const val LOWEST_PAGE_SIZE_EXPONENT = 9
        const val HIGHEST_PAGE_SIZE_EXPONENT = 16
    }

    @NotThreadSafe
    class Builder(
        private var journalMode: SQLiteJournalMode = SQLiteJournalMode.WAL,
        private var pageSizeExponent: Int = 12,
        private var trace: SQLiteTraceEventMode? = null
    ) {
        fun build() = SQLiteOpenParams(
            journalMode,
            pageSizeExponent,
            trace
        )

        fun setJournalMode(journalMode: SQLiteJournalMode) = apply {
            this.journalMode = journalMode
        }

        fun setPageSizeExponent(pageSizeExponent: Int) = apply {
            this.pageSizeExponent = pageSizeExponent
        }

        fun setTraceEventMode(trace: SQLiteTraceEventMode?) = apply {
            this.trace = trace
        }
    }
}

interface ISQLiteOpenHelper : Closeable {
    val writableDatabase: SQLiteDatabase

    val readableDatabase: SQLiteDatabase
        get() = writableDatabase

    val databaseName: String

    interface Callback {
        fun onConfigure(database: SQLiteDatabase) = Unit

        fun onCreate(database: SQLiteDatabase)

        fun onDowngrade(
            database: SQLiteDatabase,
            oldVersion: Int,
            newVersion: Int
        ): Unit = throw SQLiteException("Unable to downgrade from version $oldVersion to $newVersion.")

        fun onOpen(database: SQLiteDatabase) = Unit

        fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int)
    }

    data class Configuration(
        val callback: Callback,
        val name: String
    )
}
