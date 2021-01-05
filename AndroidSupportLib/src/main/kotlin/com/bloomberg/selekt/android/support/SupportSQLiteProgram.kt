/*
 * Copyright 2021 Bloomberg Finance L.P.
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

import androidx.sqlite.db.SupportSQLiteProgram
import com.bloomberg.selekt.ISQLProgram

internal fun ISQLProgram.asSupportSQLiteProgram() = object : SupportSQLiteProgram {
    override fun bindBlob(index: Int, value: ByteArray) = this@asSupportSQLiteProgram.bindBlob(index, value)

    override fun bindDouble(index: Int, value: Double) = this@asSupportSQLiteProgram.bindDouble(index, value)

    override fun bindLong(index: Int, value: Long) = this@asSupportSQLiteProgram.bindLong(index, value)

    override fun bindNull(index: Int) = this@asSupportSQLiteProgram.bindNull(index)

    override fun bindString(index: Int, value: String) = this@asSupportSQLiteProgram.bindString(index, value)

    override fun clearBindings() = this@asSupportSQLiteProgram.clearBindings()

    override fun close() = this@asSupportSQLiteProgram.close()
}
