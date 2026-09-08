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

package com.bloomberg.selekt.android.support

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.android.ISQLiteOpenHelper
import com.bloomberg.selekt.android.SQLiteDatabase
import com.bloomberg.selekt.android.SQLiteOpenHelper
import com.bloomberg.selekt.android.SQLiteOpenParams
import com.bloomberg.selekt.annotations.DelicateApi
import com.bloomberg.selekt.commons.zero
import java.io.Closeable
import javax.annotation.concurrent.ThreadSafe

/**
 * Creates a legacy Room factory for Selekt-backed databases.
 *
 * The supplied [key] remains owned by the caller and is never modified. The returned factory snapshots the key before
 * this function returns, so the caller should clear the supplied array immediately afterwards. For deterministic cleanup
 * of the factory's private snapshot, construct and close [SupportSQLiteOpenHelperFactory] directly.
 */
fun createSupportSQLiteOpenHelperFactory(
    journalMode: SQLiteJournalMode,
    key: ByteArray?
): SupportSQLiteOpenHelper.Factory = SupportSQLiteOpenHelperFactory(journalMode, key)

/**
 * A legacy Room factory that owns a private snapshot of its optional database key.
 *
 * The supplied [key] remains owned by the caller and is never modified. This factory snapshots it during construction;
 * callers should clear their array immediately afterwards. Close the factory as soon as no more helpers will be created.
 * Closing zeroes the factory's snapshot and prevents new helpers from being created, but does not affect existing helpers
 * because each owns a separate copy.
 *
 * @since 1.3.2
 */
@ThreadSafe
class SupportSQLiteOpenHelperFactory(
    private val journalMode: SQLiteJournalMode,
    key: ByteArray?
) : SupportSQLiteOpenHelper.Factory, Closeable {
    private val lifecycleLock = Any()
    private val key = key?.copyOf()
    private var closed = false

    override fun create(configuration: SupportSQLiteOpenHelper.Configuration) = synchronized(lifecycleLock) {
        check(!closed) { "Factory is closed." }
        SQLiteOpenHelper(
            configuration = configuration.asSelektConfiguration(key),
            context = configuration.context,
            openParams = SQLiteOpenParams(journalMode),
            version = configuration.callback.version
        ).asSupportSQLiteOpenHelper()
    }

    override fun close() = synchronized(lifecycleLock) {
        if (!closed) {
            closed = true
            key?.zero()
        }
    }
}

@JvmSynthetic
internal fun ISQLiteOpenHelper.asSupportSQLiteOpenHelper() = @DelicateApi object : SupportSQLiteOpenHelper {
    private val database: SupportSQLiteDatabase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        this@asSupportSQLiteOpenHelper.writableDatabase.asSupportSQLiteDatabase()
    }

    override fun close() = this@asSupportSQLiteOpenHelper.close()

    override val databaseName: String
        get() = this@asSupportSQLiteOpenHelper.databaseName

    override val readableDatabase: SupportSQLiteDatabase
        get() = database

    override val writableDatabase: SupportSQLiteDatabase
        get() = database

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        database.apply {
            if (enabled) {
                enableWriteAheadLogging()
            } else {
                disableWriteAheadLogging()
            }
        }
    }
}

@JvmSynthetic
internal fun SupportSQLiteOpenHelper.Configuration.asSelektConfiguration(
    key: ByteArray?
) = ISQLiteOpenHelper.Configuration(
    callback = callback.asSelektCallback(),
    key = key,
    name = requireNotNull(name) { "Encryption of in-memory SupportDatabases is not supported." }
)

@JvmSynthetic
internal fun SupportSQLiteOpenHelper.Callback.asSelektCallback() = @DelicateApi object : ISQLiteOpenHelper.Callback {
    override fun onConfigure(database: SQLiteDatabase) = this@asSelektCallback.onConfigure(
        database.asSupportSQLiteDatabase())

    override fun onCreate(database: SQLiteDatabase) = this@asSelektCallback.onCreate(
        database.asSupportSQLiteDatabase())

    override fun onDowngrade(
        database: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) = this@asSelektCallback.onDowngrade(database.asSupportSQLiteDatabase(), oldVersion, newVersion)

    override fun onUpgrade(
        database: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) = this@asSelektCallback.onUpgrade(database.asSupportSQLiteDatabase(), oldVersion, newVersion)
}
