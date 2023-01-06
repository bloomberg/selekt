/*
 * Copyright 2022 Bloomberg Finance L.P.
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

import androidx.sqlite.db.SupportSQLiteStatement
import com.bloomberg.selekt.ISQLStatement

@JvmSynthetic
internal fun ISQLStatement.asSupportSQLiteStatement() = object : SupportSQLiteStatement {
    override fun bindBlob(index: Int, value: ByteArray) = this@asSupportSQLiteStatement.bindBlob(index, value)

    override fun bindLong(index: Int, value: Long) = this@asSupportSQLiteStatement.bindLong(index, value)

    override fun bindDouble(index: Int, value: Double) = this@asSupportSQLiteStatement.bindDouble(index, value)

    override fun bindNull(index: Int) = this@asSupportSQLiteStatement.bindNull(index)

    override fun bindString(index: Int, value: String) = this@asSupportSQLiteStatement.bindString(index, value)

    override fun clearBindings() = this@asSupportSQLiteStatement.clearBindings()

    override fun close() = this@asSupportSQLiteStatement.close()

    override fun execute() = this@asSupportSQLiteStatement.execute()

    override fun executeInsert() = this@asSupportSQLiteStatement.executeInsert()

    override fun executeUpdateDelete() = this@asSupportSQLiteStatement.executeUpdateDelete()

    override fun simpleQueryForLong() = this@asSupportSQLiteStatement.simpleQueryForLong()

    override fun simpleQueryForString() = this@asSupportSQLiteStatement.simpleQueryForString()
}
