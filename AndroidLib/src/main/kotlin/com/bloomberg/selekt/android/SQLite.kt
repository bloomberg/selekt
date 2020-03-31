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

package com.bloomberg.selekt.android

import android.database.sqlite.SQLiteBindOrColumnIndexOutOfRangeException
import android.database.sqlite.SQLiteBlobTooBigException
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteDatatypeMismatchException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteMisuseException
import android.database.sqlite.SQLiteReadOnlyDatabaseException
import android.database.sqlite.SQLiteTableLockedException
import com.bloomberg.selekt.SQLCode
import com.bloomberg.selekt.SQL_AUTH
import com.bloomberg.selekt.SQL_BUSY
import com.bloomberg.selekt.SQL_CANT_OPEN
import com.bloomberg.selekt.SQL_CORRUPT
import com.bloomberg.selekt.SQL_IO_ERROR
import com.bloomberg.selekt.SQL_LOCKED
import com.bloomberg.selekt.SQL_MISMATCH
import com.bloomberg.selekt.SQL_MISUSE
import com.bloomberg.selekt.SQL_NOT_A_DATABASE
import com.bloomberg.selekt.SQL_NOT_FOUND
import com.bloomberg.selekt.SQL_OK
import com.bloomberg.selekt.SQL_RANGE
import com.bloomberg.selekt.SQL_READONLY
import com.bloomberg.selekt.SQL_TOO_BIG

object Selekt {
    fun gitCommit() = sqlite.gitCommit()

    fun sqliteLibVersion() = sqlite.libVersion()

    fun sqliteLibVersionNumber() = sqlite.libVersionNumber()
}

internal object SQLite : com.bloomberg.selekt.SQLite(sqlite) {
    override fun throwSQLException(
        code: SQLCode,
        extendedCode: SQLCode,
        message: String,
        context: String?
    ): Nothing {
        require(code != SQL_OK) { "Result code is not an error: $code" }
        val exceptionMessage = extendedErrorMessage(code, extendedCode, message, context)
        throw when (code) {
            SQL_BUSY -> SQLiteDatabaseLockedException(exceptionMessage)
            SQL_IO_ERROR -> SQLiteDiskIOException(exceptionMessage)
            SQL_READONLY -> SQLiteReadOnlyDatabaseException(exceptionMessage)
            SQL_CORRUPT, SQL_NOT_A_DATABASE -> SQLiteDatabaseCorruptException(exceptionMessage)
            SQL_MISUSE -> SQLiteMisuseException(exceptionMessage)
            SQL_LOCKED -> SQLiteTableLockedException(exceptionMessage)
            SQL_NOT_FOUND, SQL_AUTH, SQL_CANT_OPEN -> SQLiteCantOpenDatabaseException(exceptionMessage)
            SQL_MISMATCH -> SQLiteDatatypeMismatchException(exceptionMessage)
            SQL_TOO_BIG -> SQLiteBlobTooBigException(message)
            SQL_RANGE -> SQLiteBindOrColumnIndexOutOfRangeException(exceptionMessage)
            else -> SQLiteException(exceptionMessage)
        }
    }

    private fun extendedErrorMessage(
        code: SQLCode,
        extendedCode: SQLCode,
        message: String,
        context: String?
    ) = "Code $code; Extended code $extendedCode}; Message: $message; Context: $context"
}
