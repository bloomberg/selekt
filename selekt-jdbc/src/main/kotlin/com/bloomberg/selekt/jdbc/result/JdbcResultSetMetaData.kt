/*
 * Copyright 2026 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.jdbc.result

import com.bloomberg.selekt.ICursor
import com.bloomberg.selekt.jdbc.util.TypeMapping
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Types
import javax.annotation.concurrent.NotThreadSafe

/**
 * @since 0.28.0
 */
@NotThreadSafe
internal class JdbcResultSetMetaData(
    private val cursor: ICursor
) : ResultSetMetaData {
    companion object {
        private const val BIGINT_DISPLAY_SIZE = 20
        private const val DOUBLE_DISPLAY_SIZE = 24
    }

    override fun getColumnCount(): Int = cursor.columnCount

    override fun isAutoIncrement(column: Int): Boolean {
        validateColumnIndex(column)
        return false
    }

    override fun isCaseSensitive(column: Int): Boolean {
        validateColumnIndex(column)
        return getColumnType(column) == Types.VARCHAR
    }

    override fun isSearchable(column: Int): Boolean {
        validateColumnIndex(column)
        return true
    }

    override fun isCurrency(column: Int): Boolean {
        validateColumnIndex(column)
        return false
    }

    override fun isNullable(column: Int): Int {
        validateColumnIndex(column)
        return ResultSetMetaData.columnNullableUnknown
    }

    override fun isSigned(column: Int): Boolean {
        validateColumnIndex(column)
        return when (getColumnType(column)) {
            Types.BIGINT, Types.DOUBLE -> true
            else -> false
        }
    }

    override fun getColumnDisplaySize(column: Int): Int {
        validateColumnIndex(column)
        return when (getColumnType(column)) {
            Types.BIGINT -> BIGINT_DISPLAY_SIZE
            Types.DOUBLE -> DOUBLE_DISPLAY_SIZE
            else -> Integer.MAX_VALUE
        }
    }

    override fun getColumnLabel(column: Int): String {
        validateColumnIndex(column)
        return cursor.columnName(column - 1)
    }

    override fun getColumnName(column: Int): String {
        validateColumnIndex(column)
        return cursor.columnName(column - 1)
    }

    override fun getSchemaName(column: Int): String {
        validateColumnIndex(column)
        return ""
    }

    override fun getPrecision(column: Int): Int {
        validateColumnIndex(column)
        return TypeMapping.getPrecision(getColumnType(column))
    }

    override fun getScale(column: Int): Int {
        validateColumnIndex(column)
        return TypeMapping.getScale(getColumnType(column))
    }

    override fun getTableName(column: Int): String {
        validateColumnIndex(column)
        return ""
    }

    override fun getCatalogName(column: Int): String {
        validateColumnIndex(column)
        return ""
    }

    override fun getColumnType(column: Int): Int {
        validateColumnIndex(column)
        return if (hasCurrentRow()) {
            TypeMapping.toJdbcType(cursor.type(column - 1))
        } else {
            Types.VARCHAR
        }
    }

    private fun hasCurrentRow(): Boolean = cursor.isForwardOnly ||
        cursor.position() >= 0 && !cursor.isBeforeFirst() && !cursor.isAfterLast()

    override fun getColumnTypeName(column: Int): String {
        validateColumnIndex(column)
        return TypeMapping.getJdbcTypeName(getColumnType(column))
    }

    override fun isReadOnly(column: Int): Boolean {
        validateColumnIndex(column)
        return true
    }

    override fun isWritable(column: Int): Boolean {
        validateColumnIndex(column)
        return false
    }

    override fun isDefinitelyWritable(column: Int): Boolean {
        validateColumnIndex(column)
        return false
    }

    override fun getColumnClassName(column: Int): String {
        validateColumnIndex(column)
        val jdbcType = getColumnType(column)
        return TypeMapping.getJavaClassName(jdbcType)
    }

    override fun <T> unwrap(iface: Class<T>): T = if (iface.isAssignableFrom(this::class.java)) {
        @Suppress("UNCHECKED_CAST")
        this as T
    } else if (iface.isAssignableFrom(ICursor::class.java)) {
        @Suppress("UNCHECKED_CAST")
        cursor as T
    } else {
        throw SQLException("Cannot unwrap to ${iface.name}")
    }

    override fun isWrapperFor(iface: Class<*>): Boolean = iface.isAssignableFrom(this::class.java) ||
        iface.isAssignableFrom(ICursor::class.java)

    private fun validateColumnIndex(column: Int) {
        if (column !in 1..cursor.columnCount) {
            throw SQLException("Column index $column is out of range (1, ${cursor.columnCount})")
        }
    }
}
