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

import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.Array
import java.sql.Blob
import java.sql.Clob
import java.sql.Date
import java.sql.NClob
import java.sql.Ref
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.RowId
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.sql.SQLWarning
import java.sql.SQLXML
import java.sql.Statement
import java.sql.Time
import java.sql.Timestamp
import java.util.Calendar

@Suppress("Detekt.MethodOverloading")
internal class GeneratedKeysResultSet(
    private val generatedKey: Long,
    private val statement: Statement
) : ResultSet {
    private var position = 0
    private var closed = false

    private fun checkClosed() {
        if (closed) {
            throw SQLException("ResultSet is closed")
        }
    }

    private val hasRow = generatedKey >= 0L

    override fun next(): Boolean {
        checkClosed()
        return if (hasRow && position == 0) {
            position = 1
            true
        } else {
            if (position == 1) {
                position = 2
            }
            false
        }
    }

    override fun close() {
        closed = true
    }

    override fun wasNull(): Boolean = false

    override fun getString(columnIndex: Int): String {
        checkClosed()
        if (position == 0) {
            throw SQLException("Before first row")
        }
        if (columnIndex != 1) {
            throw SQLException("Invalid column index: $columnIndex")
        }
        return generatedKey.toString()
    }

    override fun getString(columnLabel: String): String = getString(1)

    override fun getBoolean(columnIndex: Int): Boolean = getLong(columnIndex) != 0L

    override fun getBoolean(columnLabel: String): Boolean = getBoolean(1)

    override fun getByte(columnIndex: Int): Byte = getLong(columnIndex).toByte()

    override fun getByte(columnLabel: String): Byte = getByte(1)

    override fun getShort(columnIndex: Int): Short = getLong(columnIndex).toShort()

    override fun getShort(columnLabel: String): Short = getShort(1)

    override fun getInt(columnIndex: Int): Int = getLong(columnIndex).toInt()

    override fun getInt(columnLabel: String): Int = getInt(1)

    override fun getLong(columnIndex: Int): Long {
        checkClosed()
        if (position == 0) {
            throw SQLException("Before first row")
        }
        if (columnIndex != 1) {
            throw SQLException("Invalid column index: $columnIndex")
        }
        return generatedKey
    }

    override fun getLong(columnLabel: String): Long = getLong(1)

    override fun getFloat(columnIndex: Int): Float = getLong(columnIndex).toFloat()

    override fun getFloat(columnLabel: String): Float = getFloat(1)

    override fun getDouble(columnIndex: Int): Double = getLong(columnIndex).toDouble()

    override fun getDouble(columnLabel: String): Double = getDouble(1)

    @Deprecated("Deprecated in Java")
    override fun getBigDecimal(columnIndex: Int, scale: Int): BigDecimal = BigDecimal(generatedKey)

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun getBigDecimal(columnLabel: String, scale: Int): BigDecimal = getBigDecimal(1, scale)

    override fun getBigDecimal(columnIndex: Int): BigDecimal = BigDecimal(generatedKey)

    override fun getBigDecimal(columnLabel: String): BigDecimal = getBigDecimal(1)

    override fun getBytes(columnIndex: Int): ByteArray? = null

    override fun getBytes(columnLabel: String): ByteArray? = null

    override fun getDate(columnIndex: Int): Date? = null

    override fun getDate(columnLabel: String): Date? = null

    override fun getDate(columnIndex: Int, cal: Calendar?): Date? = null

    override fun getDate(columnLabel: String, cal: Calendar?): Date? = null

    override fun getTime(columnIndex: Int): Time? = null

    override fun getTime(columnLabel: String): Time? = null

    override fun getTime(columnIndex: Int, cal: Calendar?): Time? = null

    override fun getTime(columnLabel: String, cal: Calendar?): Time? = null

    override fun getTimestamp(columnIndex: Int): Timestamp? = null

    override fun getTimestamp(columnLabel: String): Timestamp? = null

    override fun getTimestamp(columnIndex: Int, cal: Calendar?): Timestamp? = null

    override fun getTimestamp(columnLabel: String, cal: Calendar?): Timestamp? = null

    override fun getAsciiStream(columnIndex: Int): InputStream? = null

    override fun getAsciiStream(columnLabel: String): InputStream? = null

    @Deprecated("Deprecated in Java")
    override fun getUnicodeStream(columnIndex: Int): InputStream? = null

    @Deprecated("Deprecated in Java")
    override fun getUnicodeStream(columnLabel: String): InputStream? = null

    override fun getBinaryStream(columnIndex: Int): InputStream? = null

    override fun getBinaryStream(columnLabel: String): InputStream? = null

    override fun getWarnings(): SQLWarning? = null

    override fun clearWarnings() = Unit

    override fun getCursorName(): String = throw SQLFeatureNotSupportedException()

    override fun getMetaData(): ResultSetMetaData = throw SQLFeatureNotSupportedException()

    override fun getObject(columnIndex: Int): Any = generatedKey

    override fun getObject(columnLabel: String): Any = generatedKey

    override fun getObject(columnIndex: Int, map: Map<String, Class<*>>?): Any = generatedKey

    override fun getObject(columnLabel: String, map: Map<String, Class<*>>?): Any = generatedKey

    override fun <T> getObject(columnIndex: Int, type: Class<T>?): T {
        @Suppress("UNCHECKED_CAST")
        return generatedKey as T
    }

    override fun <T> getObject(columnLabel: String, type: Class<T>?): T = getObject(1, type)

    override fun findColumn(columnLabel: String): Int = 1

    override fun getCharacterStream(columnIndex: Int): Reader? = null

    override fun getCharacterStream(columnLabel: String): Reader? = null

    override fun isBeforeFirst(): Boolean = position == 0

    override fun isAfterLast(): Boolean = position > 1

    override fun isFirst(): Boolean = position == 1

    override fun isLast(): Boolean = position == 1

    override fun beforeFirst() {
        checkClosed()
        position = 0
    }

    override fun afterLast() {
        checkClosed()
        position = 2
    }

    override fun first(): Boolean {
        checkClosed()
        position = 1
        return true
    }

    override fun last(): Boolean = first()

    override fun getRow(): Int = if (position == 1) 1 else 0

    override fun absolute(row: Int): Boolean {
        checkClosed()
        return when (row) {
            1 -> {
                position = 1
                true
            }
            else -> {
                position = 2
                false
            }
        }
    }

    override fun relative(rows: Int): Boolean {
        checkClosed()
        position += rows
        return position == 1
    }

    override fun previous(): Boolean {
        checkClosed()
        if (position > 0) position--
        return position == 1
    }

    override fun setFetchDirection(direction: Int) {
        if (direction != ResultSet.FETCH_FORWARD) {
            throw SQLFeatureNotSupportedException()
        }
    }

    override fun getFetchDirection(): Int = ResultSet.FETCH_FORWARD

    override fun setFetchSize(rows: Int) = Unit

    override fun getFetchSize(): Int = 1

    override fun getType(): Int = ResultSet.TYPE_FORWARD_ONLY

    override fun getConcurrency(): Int = ResultSet.CONCUR_READ_ONLY

    override fun rowUpdated(): Boolean = false

    override fun rowInserted(): Boolean = false

    override fun rowDeleted(): Boolean = false

    override fun updateNull(columnIndex: Int) = throw SQLFeatureNotSupportedException()

    override fun updateNull(columnLabel: String) = throw SQLFeatureNotSupportedException()

    override fun updateBoolean(columnIndex: Int, x: Boolean) = throw SQLFeatureNotSupportedException()

    override fun updateBoolean(columnLabel: String, x: Boolean) = throw SQLFeatureNotSupportedException()

    override fun updateByte(columnIndex: Int, x: Byte) = throw SQLFeatureNotSupportedException()

    override fun updateByte(columnLabel: String, x: Byte) = throw SQLFeatureNotSupportedException()

    override fun updateShort(columnIndex: Int, x: Short) = throw SQLFeatureNotSupportedException()

    override fun updateShort(columnLabel: String, x: Short) = throw SQLFeatureNotSupportedException()

    override fun updateInt(columnIndex: Int, x: Int) = throw SQLFeatureNotSupportedException()

    override fun updateInt(columnLabel: String, x: Int) = throw SQLFeatureNotSupportedException()

    override fun updateLong(columnIndex: Int, x: Long) = throw SQLFeatureNotSupportedException()

    override fun updateLong(columnLabel: String, x: Long) = throw SQLFeatureNotSupportedException()

    override fun updateFloat(columnIndex: Int, x: Float) = throw SQLFeatureNotSupportedException()

    override fun updateFloat(columnLabel: String, x: Float) = throw SQLFeatureNotSupportedException()

    override fun updateDouble(columnIndex: Int, x: Double) = throw SQLFeatureNotSupportedException()

    override fun updateDouble(columnLabel: String, x: Double) = throw SQLFeatureNotSupportedException()

    override fun updateBigDecimal(columnIndex: Int, x: BigDecimal?) = throw SQLFeatureNotSupportedException()

    override fun updateBigDecimal(columnLabel: String, x: BigDecimal?) = throw SQLFeatureNotSupportedException()

    override fun updateString(columnIndex: Int, x: String?) = throw SQLFeatureNotSupportedException()

    override fun updateString(columnLabel: String, x: String?) = throw SQLFeatureNotSupportedException()

    override fun updateBytes(columnIndex: Int, x: ByteArray?) = throw SQLFeatureNotSupportedException()

    override fun updateBytes(columnLabel: String, x: ByteArray?) = throw SQLFeatureNotSupportedException()

    override fun updateDate(columnIndex: Int, x: Date?) = throw SQLFeatureNotSupportedException()

    override fun updateDate(columnLabel: String, x: Date?) = throw SQLFeatureNotSupportedException()

    override fun updateTime(columnIndex: Int, x: Time?) = throw SQLFeatureNotSupportedException()

    override fun updateTime(columnLabel: String, x: Time?) = throw SQLFeatureNotSupportedException()

    override fun updateTimestamp(columnIndex: Int, x: Timestamp?) = throw SQLFeatureNotSupportedException()

    override fun updateTimestamp(columnLabel: String, x: Timestamp?) = throw SQLFeatureNotSupportedException()

    override fun updateAsciiStream(columnIndex: Int, x: InputStream?, length: Int) = throw SQLFeatureNotSupportedException()

    override fun updateAsciiStream(
        columnLabel: String,
        x: InputStream?,
        length: Int
    ) = throw SQLFeatureNotSupportedException()

    override fun updateAsciiStream(columnIndex: Int, x: InputStream?, length: Long) = throw SQLFeatureNotSupportedException()

    override fun updateAsciiStream(
        columnLabel: String,
        x: InputStream?,
        length: Long
    ) = throw SQLFeatureNotSupportedException()

    override fun updateAsciiStream(columnIndex: Int, x: InputStream?) = throw SQLFeatureNotSupportedException()

    override fun updateAsciiStream(columnLabel: String, x: InputStream?) = throw SQLFeatureNotSupportedException()

    override fun updateBinaryStream(columnIndex: Int, x: InputStream?, length: Int) = throw SQLFeatureNotSupportedException()

    override fun updateBinaryStream(
        columnLabel: String,
        x: InputStream?,
        length: Int
    ) = throw SQLFeatureNotSupportedException()

    override fun updateBinaryStream(
        columnIndex: Int,
        x: InputStream?,
        length: Long
    ) = throw SQLFeatureNotSupportedException()

    override fun updateBinaryStream(
        columnLabel: String,
        x: InputStream?,
        length: Long
    ) = throw SQLFeatureNotSupportedException()

    override fun updateBinaryStream(columnIndex: Int, x: InputStream?) = throw SQLFeatureNotSupportedException()

    override fun updateBinaryStream(columnLabel: String, x: InputStream?) = throw SQLFeatureNotSupportedException()

    override fun updateCharacterStream(columnIndex: Int, x: Reader?, length: Int) = throw SQLFeatureNotSupportedException()

    override fun updateCharacterStream(
        columnLabel: String,
        reader: Reader?,
        length: Int
    ) = throw SQLFeatureNotSupportedException()

    override fun updateCharacterStream(columnIndex: Int, x: Reader?, length: Long) = throw SQLFeatureNotSupportedException()

    override fun updateCharacterStream(
        columnLabel: String,
        reader: Reader?,
        length: Long
    ) = throw SQLFeatureNotSupportedException()

    override fun updateCharacterStream(columnIndex: Int, x: Reader?) = throw SQLFeatureNotSupportedException()

    override fun updateCharacterStream(columnLabel: String, reader: Reader?) = throw SQLFeatureNotSupportedException()

    override fun updateObject(columnIndex: Int, x: Any?, scaleOrLength: Int) = throw SQLFeatureNotSupportedException()

    override fun updateObject(columnIndex: Int, x: Any?) = throw SQLFeatureNotSupportedException()

    override fun updateObject(columnLabel: String, x: Any?, scaleOrLength: Int) = throw SQLFeatureNotSupportedException()

    override fun updateObject(columnLabel: String, x: Any?) = throw SQLFeatureNotSupportedException()

    override fun insertRow() = throw SQLFeatureNotSupportedException()

    override fun updateRow() = throw SQLFeatureNotSupportedException()

    override fun deleteRow() = throw SQLFeatureNotSupportedException()

    override fun refreshRow() = throw SQLFeatureNotSupportedException()

    override fun cancelRowUpdates() = throw SQLFeatureNotSupportedException()

    override fun moveToInsertRow() = throw SQLFeatureNotSupportedException()

    override fun moveToCurrentRow() = throw SQLFeatureNotSupportedException()

    override fun getStatement(): Statement = statement

    override fun getRef(columnIndex: Int): Ref? = null

    override fun getRef(columnLabel: String): Ref? = null

    override fun getBlob(columnIndex: Int): Blob? = null

    override fun getBlob(columnLabel: String): Blob? = null

    override fun getClob(columnIndex: Int): Clob? = null

    override fun getClob(columnLabel: String): Clob? = null

    override fun getArray(columnIndex: Int): Array? = null

    override fun getArray(columnLabel: String): Array? = null

    override fun getURL(columnIndex: Int): URL? = null

    override fun getURL(columnLabel: String): URL? = null

    override fun updateRef(columnIndex: Int, x: Ref?) = throw SQLFeatureNotSupportedException()

    override fun updateRef(columnLabel: String, x: Ref?) = throw SQLFeatureNotSupportedException()

    override fun updateBlob(columnIndex: Int, x: Blob?) = throw SQLFeatureNotSupportedException()

    override fun updateBlob(columnLabel: String, x: Blob?) = throw SQLFeatureNotSupportedException()

    override fun updateBlob(
        columnIndex: Int,
        inputStream: InputStream?,
        length: Long
    ) = throw SQLFeatureNotSupportedException()

    override fun updateBlob(
        columnLabel: String,
        inputStream: InputStream?,
        length: Long
    ) = throw SQLFeatureNotSupportedException()

    override fun updateBlob(columnIndex: Int, inputStream: InputStream?) = throw SQLFeatureNotSupportedException()

    override fun updateBlob(columnLabel: String, inputStream: InputStream?) = throw SQLFeatureNotSupportedException()

    override fun updateClob(columnIndex: Int, x: Clob?) = throw SQLFeatureNotSupportedException()

    override fun updateClob(columnLabel: String, x: Clob?) = throw SQLFeatureNotSupportedException()

    override fun updateClob(columnIndex: Int, reader: Reader?, length: Long) = throw SQLFeatureNotSupportedException()

    override fun updateClob(columnLabel: String, reader: Reader?, length: Long) = throw SQLFeatureNotSupportedException()

    override fun updateClob(columnIndex: Int, reader: Reader?) = throw SQLFeatureNotSupportedException()

    override fun updateClob(columnLabel: String, reader: Reader?) = throw SQLFeatureNotSupportedException()

    override fun updateArray(columnIndex: Int, x: Array?) = throw SQLFeatureNotSupportedException()

    override fun updateArray(columnLabel: String, x: Array?) = throw SQLFeatureNotSupportedException()

    override fun getRowId(columnIndex: Int): RowId? = null

    override fun getRowId(columnLabel: String): RowId? = null

    override fun updateRowId(columnIndex: Int, x: RowId?) = throw SQLFeatureNotSupportedException()

    override fun updateRowId(columnLabel: String, x: RowId?) = throw SQLFeatureNotSupportedException()

    override fun getHoldability(): Int = ResultSet.CLOSE_CURSORS_AT_COMMIT

    override fun isClosed(): Boolean = closed

    override fun updateNString(columnIndex: Int, nString: String?) = throw SQLFeatureNotSupportedException()

    override fun updateNString(columnLabel: String, nString: String?) = throw SQLFeatureNotSupportedException()

    override fun updateNClob(columnIndex: Int, nClob: NClob?) = throw SQLFeatureNotSupportedException()

    override fun updateNClob(columnLabel: String, nClob: NClob?) = throw SQLFeatureNotSupportedException()

    override fun updateNClob(columnIndex: Int, reader: Reader?, length: Long) = throw SQLFeatureNotSupportedException()

    override fun updateNClob(columnLabel: String, reader: Reader?, length: Long) = throw SQLFeatureNotSupportedException()

    override fun updateNClob(columnIndex: Int, reader: Reader?) = throw SQLFeatureNotSupportedException()

    override fun updateNClob(columnLabel: String, reader: Reader?) = throw SQLFeatureNotSupportedException()

    override fun getNClob(columnIndex: Int): NClob? = null

    override fun getNClob(columnLabel: String): NClob? = null

    override fun getSQLXML(columnIndex: Int): SQLXML? = null

    override fun getSQLXML(columnLabel: String): SQLXML? = null

    override fun updateSQLXML(columnIndex: Int, xmlObject: SQLXML?) = throw SQLFeatureNotSupportedException()

    override fun updateSQLXML(columnLabel: String, xmlObject: SQLXML?) = throw SQLFeatureNotSupportedException()

    override fun getNString(columnIndex: Int): String? = null

    override fun getNString(columnLabel: String): String? = null

    override fun getNCharacterStream(columnIndex: Int): Reader? = null

    override fun getNCharacterStream(columnLabel: String): Reader? = null

    override fun updateNCharacterStream(columnIndex: Int, x: Reader?, length: Long) = throw SQLFeatureNotSupportedException()

    override fun updateNCharacterStream(
        columnLabel: String,
        reader: Reader?,
        length: Long
    ) = throw SQLFeatureNotSupportedException()

    override fun updateNCharacterStream(columnIndex: Int, x: Reader?) = throw SQLFeatureNotSupportedException()

    override fun updateNCharacterStream(columnLabel: String, reader: Reader?) = throw SQLFeatureNotSupportedException()

    override fun <T> unwrap(iface: Class<T>?): T {
        if (iface?.isAssignableFrom(this::class.java) == true) {
            @Suppress("UNCHECKED_CAST")
            return this as T
        }
        throw SQLException("Cannot unwrap to ${iface?.name}")
    }

    override fun isWrapperFor(iface: Class<*>?): Boolean = iface?.isAssignableFrom(this::class.java) == true
}
