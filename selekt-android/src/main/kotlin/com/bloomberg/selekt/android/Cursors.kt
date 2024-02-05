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

import android.database.AbstractCursor
import android.database.Cursor
import com.bloomberg.selekt.ColumnType
import com.bloomberg.selekt.ICursor
import javax.annotation.concurrent.NotThreadSafe

private fun ColumnType.asAndroidCursorFieldType() = when (this) {
    ColumnType.STRING -> Cursor.FIELD_TYPE_STRING
    ColumnType.INTEGER -> Cursor.FIELD_TYPE_INTEGER
    ColumnType.NULL -> Cursor.FIELD_TYPE_NULL
    ColumnType.FLOAT -> Cursor.FIELD_TYPE_FLOAT
    ColumnType.BLOB -> Cursor.FIELD_TYPE_BLOB
}

@JvmSynthetic
internal fun ICursor.asAndroidCursor(): Cursor = CursorWrapper(this)

@NotThreadSafe
private class CursorWrapper(private val cursor: ICursor) : AbstractCursor() {
    private val columns = cursor.columnNames()

    override fun close() {
        try {
            cursor.close()
        } finally {
            super.close()
        }
    }

    override fun getBlob(index: Int) = cursor.getBlob(index)

    override fun getColumnCount(): Int = cursor.columnCount

    override fun getColumnIndex(name: String) = cursor.columnIndex(name)

    override fun getColumnIndexOrThrow(name: String) = getColumnIndex(name).also {
        require(it >= 0) { "Column index for '$name' not found." }
    }

    override fun getColumnName(index: Int) = cursor.columnName(index)

    override fun getColumnNames() = columns

    override fun getCount() = cursor.count

    override fun getDouble(index: Int) = cursor.getDouble(index)

    override fun getFloat(index: Int) = cursor.getFloat(index)

    override fun getInt(index: Int) = cursor.getInt(index)

    override fun getLong(index: Int) = cursor.getLong(index)

    override fun getShort(index: Int) = cursor.getShort(index)

    override fun getString(index: Int) = cursor.getString(index)

    override fun getType(index: Int) = cursor.type(index).asAndroidCursorFieldType()

    override fun isNull(column: Int) = cursor.isNull(column)

    override fun onMove(oldPosition: Int, newPosition: Int) = cursor.moveToPosition(newPosition)
}
