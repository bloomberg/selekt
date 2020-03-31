/*
 * Copyright 2020 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

internal fun createSupportSQLiteOpenHelperFactory(
    journalMode: SQLiteJournalMode,
    key: ByteArray?
): SupportSQLiteOpenHelper.Factory = SupportSQLiteOpenHelperFactory(journalMode, key)

private class SupportSQLiteOpenHelperFactory(
    private val journalMode: SQLiteJournalMode,
    private val key: ByteArray?
) : SupportSQLiteOpenHelper.Factory {
    override fun create(configuration: SupportSQLiteOpenHelper.Configuration) = SQLiteOpenHelper(
        configuration = configuration.asSelektConfiguration(key),
        context = configuration.context,
        openParams = SQLiteOpenParams(journalMode),
        version = configuration.callback.version
    ).asSupportSQLiteOpenHelper()
}

internal fun ISQLiteOpenHelper.asSupportSQLiteOpenHelper() = object : SupportSQLiteOpenHelper {
    private val database: SupportSQLiteDatabase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        this@asSupportSQLiteOpenHelper.writableDatabase.asSupportSQLiteDatabase()
    }

    override fun close() = this@asSupportSQLiteOpenHelper.close()

    override fun getDatabaseName() = this@asSupportSQLiteOpenHelper.databaseName

    override fun getReadableDatabase() = database

    override fun getWritableDatabase() = database

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

internal fun SupportSQLiteOpenHelper.Configuration.asSelektConfiguration(
    key: ByteArray?
) = ISQLiteOpenHelper.Configuration(
    callback = callback.asSelektCallback(),
    key = key,
    name = requireNotNull(name) { "Encryption of in-memory SupportDatabases is not supported." }
)

internal fun SupportSQLiteOpenHelper.Callback.asSelektCallback() = object : ISQLiteOpenHelper.Callback {
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
