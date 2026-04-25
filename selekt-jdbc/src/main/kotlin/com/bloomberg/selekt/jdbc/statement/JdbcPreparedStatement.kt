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

package com.bloomberg.selekt.jdbc.statement

import com.bloomberg.selekt.ISQLStatement
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.exception.SQLExceptionMapper
import com.bloomberg.selekt.jdbc.result.JdbcResultSet
import com.bloomberg.selekt.jdbc.util.TypeMapping
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.BatchUpdateException
import java.sql.Blob
import java.sql.Clob
import java.sql.Date
import java.sql.NClob
import java.sql.ParameterMetaData
import java.sql.PreparedStatement
import java.sql.Ref
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.RowId
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.sql.SQLXML
import java.sql.Statement
import java.sql.Time
import java.sql.Timestamp
import java.util.Calendar
import javax.annotation.concurrent.NotThreadSafe

private const val INITIAL_BATCH_CHUNK_SIZE = 128

@Suppress("TooGenericExceptionCaught")
@NotThreadSafe
internal open class JdbcPreparedStatement(
    connection: JdbcConnection,
    private val database: SQLDatabase,
    val sql: String,
    resultSetType: Int = ResultSet.TYPE_FORWARD_ONLY,
    resultSetConcurrency: Int = ResultSet.CONCUR_READ_ONLY,
    resultSetHoldability: Int = ResultSet.CLOSE_CURSORS_AT_COMMIT
) : JdbcStatement(connection, database, resultSetType, resultSetConcurrency, resultSetHoldability), PreparedStatement {
    @Suppress("Detekt.UseDataClass")
    private class BatchChunk(val capacity: Int) {
        val data = arrayOfNulls<Array<Any?>>(capacity)
        var count = 0
        var next: BatchChunk? = null
    }

    private val parameterCount = sql.count { it == '?' }
    private val parameters = arrayOfNulls<Any?>(parameterCount)
    private var firstChunk: BatchChunk? = null
    private var currentChunk: BatchChunk? = null
    private var totalBatchCount = 0

    private fun validateParameterIndex(parameterIndex: Int) {
        checkClosed()
        if (parameterIndex !in 1..parameterCount) {
            throw SQLException("Parameter index $parameterIndex is out of range (1, $parameterCount)")
        }
    }

    override fun executeQuery(): ResultSet {
        checkClosed()
        return runCatching {
            connection.ensureTransaction()
            JdbcResultSet(
                database.query(applyMaxRows(sql), buildBindArgs()),
                this,
                resultSetType,
                resultSetConcurrency,
                resultSetHoldability
            )
        }.getOrElse { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun executeUpdate(): Int {
        checkClosed()
        return runCatching {
            connection.ensureTransaction()
            executeUpdate(database.compileStatement(sql, buildBindArgs()))
        }.getOrElse { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun execute(): Boolean {
        checkClosed()
        return runCatching {
            connection.ensureTransaction()
            val statement = database.compileStatement(sql, buildBindArgs())
            if (statement.isReadOnly) {
                executeQuery()
                true
            } else {
                executeUpdate(statement)
                false
            }
        }.getOrElse { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    private fun executeUpdate(
        statement: ISQLStatement
    ): Int = if (!statement.isReadOnly && isInsertSql(sql)) {
        lastGeneratedKey = statement.executeInsert()
        1
    } else {
        statement.executeUpdateDelete().also { _ ->
            lastGeneratedKey = -1L
        }
    }

    override fun clearParameters() {
        checkClosed()
        parameters.fill(null)
    }

    override fun addBatch() {
        checkClosed()
        if (firstChunk == null) {
            firstChunk = BatchChunk(INITIAL_BATCH_CHUNK_SIZE)
            currentChunk = firstChunk
        }
        currentChunk!!.run {
            if (count == capacity) {
                currentChunk = BatchChunk(totalBatchCount).also {
                    next = it
                }
            }
        }
        currentChunk!!.run {
            data[count] = Array(parameterCount) { i ->
                TypeMapping.convertToSQLite(parameters[i])
            }
            ++count
        }
        ++totalBatchCount
    }

    override fun clearBatch() {
        checkClosed()
        currentChunk?.let {
            for (i in 0 until it.count) {
                it.data[i]?.fill(null)
            }
            it.count = 0
            it.next = null
            firstChunk = it
        }
        totalBatchCount = 0
    }

    override fun executeBatch(): IntArray {
        checkClosed()
        if (totalBatchCount == 0) {
            return IntArray(0)
        }
        try {
            if (database.compileStatement(sql).isReadOnly) {
                throw SQLException("Read-only statements are not allowed in batch execution")
            }
            database.batch(sql, sequence {
                var chunk: BatchChunk? = firstChunk
                while (chunk != null) {
                    for (i in 0 until chunk.count) {
                        chunk.data[i]!!.let { data ->
                            yield(data)
                            data.fill(null)
                        }
                    }
                    chunk = chunk.next
                }
            })
            return IntArray(totalBatchCount) { Statement.SUCCESS_NO_INFO }
        } catch (e: Exception) {
            SQLExceptionMapper.mapException(
                e as? SQLException ?: SQLException(e.message, e)
            ).run {
                throw BatchUpdateException(
                    message ?: "Batch execution failed",
                    sqlState,
                    errorCode,
                    IntArray(totalBatchCount) { Statement.EXECUTE_FAILED },
                    this
                )
            }
        } finally {
            clearBatch()
        }
    }

    private fun buildBindArgs(): Array<out Any?> = Array(parameterCount) { i ->
        TypeMapping.convertToSQLite(parameters[i])
    }

    override fun setNull(parameterIndex: Int, sqlType: Int) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = null
    }

    override fun setNull(parameterIndex: Int, sqlType: Int, typeName: String?) {
        setNull(parameterIndex, sqlType)
    }

    override fun setBoolean(parameterIndex: Int, x: Boolean) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setByte(parameterIndex: Int, x: Byte) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setShort(parameterIndex: Int, x: Short) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setInt(parameterIndex: Int, x: Int) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setLong(parameterIndex: Int, x: Long) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setFloat(parameterIndex: Int, x: Float) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setDouble(parameterIndex: Int, x: Double) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setBigDecimal(parameterIndex: Int, x: BigDecimal?) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setString(parameterIndex: Int, x: String?) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setBytes(parameterIndex: Int, x: ByteArray?) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setDate(parameterIndex: Int, x: Date?) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x?.toString()
    }

    override fun setDate(parameterIndex: Int, x: Date?, cal: Calendar?) {
        setDate(parameterIndex, x)
    }

    override fun setTime(parameterIndex: Int, x: Time?) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x?.toString()
    }

    override fun setTime(parameterIndex: Int, x: Time?, cal: Calendar?) {
        setTime(parameterIndex, x)
    }

    override fun setTimestamp(parameterIndex: Int, x: Timestamp?) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x?.toString()
    }

    override fun setTimestamp(parameterIndex: Int, x: Timestamp?, cal: Calendar?) {
        setTimestamp(parameterIndex, x)
    }

    override fun setObject(parameterIndex: Int, x: Any?) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setObject(parameterIndex: Int, x: Any?, targetSqlType: Int) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setObject(parameterIndex: Int, x: Any?, targetSqlType: Int, scaleOrLength: Int) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x
    }

    override fun setAsciiStream(parameterIndex: Int, x: InputStream?, length: Int) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x?.readBytes()?.toString(Charsets.US_ASCII)
    }

    override fun setAsciiStream(parameterIndex: Int, x: InputStream?, length: Long) {
        setAsciiStream(parameterIndex, x, length.toInt())
    }

    override fun setAsciiStream(parameterIndex: Int, x: InputStream?) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x?.readBytes()?.toString(Charsets.US_ASCII)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("setCharacterStream(parameterIndex, x, length)"))
    override fun setUnicodeStream(parameterIndex: Int, x: InputStream?, length: Int) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x?.readBytes()?.toString(Charsets.UTF_16)
    }

    override fun setBinaryStream(parameterIndex: Int, x: InputStream?, length: Int) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x?.readBytes()
    }

    override fun setBinaryStream(parameterIndex: Int, x: InputStream?, length: Long) {
        setBinaryStream(parameterIndex, x, length.toInt())
    }

    override fun setBinaryStream(parameterIndex: Int, x: InputStream?) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = x?.readBytes()
    }

    override fun setCharacterStream(parameterIndex: Int, reader: Reader?, length: Int) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = reader?.readText()
    }

    override fun setCharacterStream(parameterIndex: Int, reader: Reader?, length: Long) {
        setCharacterStream(parameterIndex, reader, length.toInt())
    }

    override fun setCharacterStream(parameterIndex: Int, reader: Reader?) {
        validateParameterIndex(parameterIndex)
        parameters[parameterIndex - 1] = reader?.readText()
    }

    override fun setRef(parameterIndex: Int, x: Ref?) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setBlob(parameterIndex: Int, x: Blob?) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setBlob(parameterIndex: Int, inputStream: InputStream?, length: Long) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setBlob(parameterIndex: Int, inputStream: InputStream?) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setClob(parameterIndex: Int, x: Clob?) {
        validateParameterIndex(parameterIndex)
        if (x == null) {
            parameters[parameterIndex - 1] = null
        } else {
            val content = x.getSubString(1, x.length().toInt())
            parameters[parameterIndex - 1] = content
        }
    }

    override fun setClob(parameterIndex: Int, reader: Reader?, length: Long) {
        validateParameterIndex(parameterIndex)
        if (reader == null) {
            parameters[parameterIndex - 1] = null
        } else {
            val content = CharArray(length.toInt())
            var totalRead = 0
            while (totalRead < length) {
                val count = reader.read(content, totalRead, (length - totalRead).toInt())
                if (count == -1) {
                    break
                }
                totalRead += count
            }
            parameters[parameterIndex - 1] = String(content, 0, totalRead)
        }
    }

    override fun setClob(parameterIndex: Int, reader: Reader?) {
        validateParameterIndex(parameterIndex)
        if (reader == null) {
            parameters[parameterIndex - 1] = null
        } else {
            parameters[parameterIndex - 1] = reader.readText()
        }
    }

    override fun setArray(parameterIndex: Int, x: java.sql.Array?) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setURL(parameterIndex: Int, x: URL?) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setRowId(parameterIndex: Int, x: RowId?) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setNString(parameterIndex: Int, value: String?) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setNCharacterStream(
        parameterIndex: Int,
        value: Reader?,
        length: Long
    ) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setNCharacterStream(parameterIndex: Int, value: Reader?) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setNClob(parameterIndex: Int, value: NClob?) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setNClob(parameterIndex: Int, reader: Reader?, length: Long) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setNClob(parameterIndex: Int, reader: Reader?) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun setSQLXML(parameterIndex: Int, xmlObject: SQLXML?) {
        validateParameterIndex(parameterIndex)
        throw SQLFeatureNotSupportedException()
    }

    override fun getMetaData(): ResultSetMetaData {
        throw SQLFeatureNotSupportedException("Metadata not available for prepared statements")
    }

    override fun getParameterMetaData(): ParameterMetaData = JdbcParameterMetaData(parameterCount, parameters)
}
