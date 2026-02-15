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

import com.bloomberg.selekt.ICursor
import com.bloomberg.selekt.ISQLStatement
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.result.JdbcResultSet
import com.bloomberg.selekt.jdbc.util.ConnectionURL
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URI
import java.sql.Blob
import java.sql.Clob
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Date
import java.sql.NClob
import java.sql.PreparedStatement
import java.sql.Ref
import java.sql.ResultSet
import java.sql.RowId
import java.sql.SQLException
import java.sql.SQLXML
import java.sql.Time
import java.sql.Timestamp
import java.sql.Types
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.mockito.kotlin.doReturn

internal class JdbcPreparedStatementTest {
    private lateinit var database: SQLDatabase
    private lateinit var connection: JdbcConnection
    private lateinit var cursor: ICursor
    private lateinit var preparedStatement: JdbcPreparedStatement

    @BeforeEach
    fun setUp() {
        database = mock<SQLDatabase>()
        cursor = mock<ICursor>()
        val connectionURL = ConnectionURL.parse("jdbc:sqlite:/tmp/test.db")
        val properties = Properties()
        connection = JdbcConnection(database, connectionURL, properties)
        val sql = "SELECT * FROM users WHERE id = ? AND name = ?"
        preparedStatement = JdbcPreparedStatement(connection, database, sql)
    }

    @AfterEach
    fun tearDown() {
        preparedStatement.close()
        connection.close()
    }

    @Test
    fun executeQuery() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        assertTrue(preparedStatement.apply {
            setInt(1, 42)
            setString(2, "test")
        }.executeQuery() is JdbcResultSet)
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun executeUpdate() {
        val mockStatement = mock<ISQLStatement>()
        whenever(database.compileStatement(any<String>(), any<Array<Any?>>())) doReturn mockStatement
        whenever(mockStatement.executeUpdateDelete()) doReturn 2
        assertEquals(2, preparedStatement.apply {
            setInt(1, 42)
            setString(2, "updated")
        }.executeUpdate())
        verify(database).compileStatement(any<String>(), any<Array<Any?>>())
        verify(mockStatement).executeUpdateDelete()
    }

    @Test
    fun execute() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        assertTrue(preparedStatement.apply {
            setInt(1, 42)
            setString(2, "test")
        }.execute())
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun executeWithUpdateStatement() {
        val mockStatement = mock<ISQLStatement>()
        whenever(database.compileStatement(any<String>(), any<Array<Any?>>())) doReturn mockStatement
        whenever(mockStatement.executeUpdateDelete()) doReturn 1
        assertFalse(JdbcPreparedStatement(
            connection,
            database,
            "UPDATE users SET name = ? WHERE id = ?"
        ).apply {
            setString(1, "updated")
            setInt(2, 42)
        }.use(PreparedStatement::execute))
        verify(database).compileStatement(any<String>(), any<Array<Any?>>())
        verify(mockStatement).executeUpdateDelete()
    }

    @Test
    fun executeQueryWithException() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doThrow RuntimeException("Query failed")
        assertFailsWith<SQLException> {
            preparedStatement.apply {
                setInt(1, 42)
                setString(2, "test")
            }.executeQuery()
        }
    }

    @Test
    fun executeUpdateWithException() {
        val mockStatement = mock<ISQLStatement>()
        whenever(database.compileStatement(any<String>(), any<Array<Any?>>())) doReturn mockStatement
        whenever(mockStatement.executeUpdateDelete()) doThrow RuntimeException("Update failed")
        assertFailsWith<SQLException> {
            preparedStatement.apply {
                setInt(1, 42)
                setString(2, "test")
            }.executeUpdate()
        }
    }

    @Test
    fun executeWithException() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doThrow RuntimeException("Execute failed")
        assertFailsWith<SQLException> {
            preparedStatement.apply {
                setInt(1, 42)
                setString(2, "test")
            }.execute()
        }
    }

    @Test
    fun parameterBinding() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        preparedStatement.run {
            setNull(1, Types.VARCHAR)
            setBoolean(2, true)
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun dateTimeParameters() {
        val sql = "SELECT * FROM events WHERE event_date = ? AND event_time = ? AND event_timestamp = ?"
        val statement = JdbcPreparedStatement(connection, database, sql)
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        statement.apply {
            setDate(1, Date.valueOf("2026-02-04"))
            setTime(2, Time.valueOf("10:30:45"))
            setTimestamp(3, Timestamp.valueOf("2026-02-04 10:30:45"))
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setObject() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        preparedStatement.apply {
            setObject(1, 42)
            setObject(2, "string")
        }.executeQuery()
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setObjectWithTargetType() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        preparedStatement.apply {
            setObject(1, "42", Types.INTEGER)
            setObject(2, 42, Types.VARCHAR)
        }.executeQuery()
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun clearParameters(): Unit = preparedStatement.run {
        setInt(1, 42)
        setString(2, "test")
        clearParameters()
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        setInt(1, 100)
        executeQuery()
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun preparedStatementReuse() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        preparedStatement.apply {
            setInt(1, 42)
            setString(2, "first")
        }.executeQuery()
        preparedStatement.apply {
            setInt(1, 100)
            setString(2, "second")
        }.executeQuery()
        preparedStatement.apply {
            clearParameters()
            setInt(1, 200)
            setString(2, "third")
        }.executeQuery()
        verify(database, times(3)).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun getParameterMetaData() {
        assertNotNull(preparedStatement.parameterMetaData)
    }

    @Test
    fun getMetaData() {
        assertFailsWith<SQLException> {
            preparedStatement.metaData
        }
    }

    @Test
    fun executeBatch() {
        val batchSql = "UPDATE users SET name = ? WHERE id = ?"
        val batchStatement = JdbcPreparedStatement(connection, database, batchSql).apply {
            setString(1, "first")
            setInt(2, 1)
            addBatch()
            setString(1, "second")
            setInt(2, 2)
            addBatch()
        }
        whenever(database.batch(any<String>(), any<Sequence<Array<Any?>>>())) doAnswer { invocation ->
            invocation.getArgument<Sequence<Array<Any?>>>(1).count()
        }
        batchStatement.executeBatch().run {
            assertEquals(2, size)
            assertEquals(-2, first())
            assertEquals(-2, this[1])
        }
        verify(database).batch(any<String>(), any<Sequence<Array<Any?>>>())
    }

    @Test
    fun executeBatchWithChunkExpansion() {
        val batchSql = "UPDATE users SET value = ? WHERE id = ?"
        val batchStatement = JdbcPreparedStatement(connection, database, batchSql).apply {
            repeat(150) { i ->
                setInt(1, i)
                setInt(2, i)
                addBatch()
            }
        }
        whenever(database.batch(any<String>(), any<Sequence<Array<Any?>>>())) doAnswer { invocation ->
            invocation.getArgument<Sequence<Array<Any?>>>(1).count()
        }
        val updateCounts = batchStatement.executeBatch()
        assertEquals(150, updateCounts.size)
        verify(database).batch(any<String>(), any<Sequence<Array<Any?>>>())
    }

    @Test
    fun invalidParameterIndex() {
        assertFailsWith<SQLException> {
            preparedStatement.setInt(0, 42)
        }
        assertFailsWith<SQLException> {
            preparedStatement.setString(-1, "test")
        }
    }

    @Test
    fun executeWithoutParameters() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        assertTrue(JdbcPreparedStatement(connection, database, "SELECT COUNT(*) FROM users").execute())
    }

    @Test
    fun unsupportedMethods(): Unit = preparedStatement.run {
        assertFailsWith<SQLException> {
            setRef(1, mock<Ref>())
        }
        assertFailsWith<SQLException> {
            setBlob(1, mock<Blob>())
        }
        assertFailsWith<SQLException> {
            setArray(1, mock<java.sql.Array>())
        }
        assertFailsWith<SQLException> {
            setURL(1, URI.create("http://example.com").toURL())
        }
        assertFailsWith<SQLException> {
            setRowId(1, mock<RowId>())
        }
        assertFailsWith<SQLException> {
            setNString(1, "test")
        }
        assertFailsWith<SQLException> {
            setNCharacterStream(1, mock<Reader>())
        }
        assertFailsWith<SQLException> {
            setNClob(1, mock<NClob>())
        }
        assertFailsWith<SQLException> {
            setSQLXML(1, mock<SQLXML>())
        }
    }

    @Test
    fun closure(): Unit = preparedStatement.run {
        assertFalse(isClosed)
        close()
        assertTrue(isClosed)
    }

    @Test
    fun operationsAfterClose(): Unit = preparedStatement.run {
        close()
        assertFailsWith<SQLException> {
            executeQuery()
        }
        assertFailsWith<SQLException> {
            setInt(1, 42)
        }
    }

    @Test
    fun wrapperInterface(): Unit = preparedStatement.run {
        assertTrue(isWrapperFor(JdbcPreparedStatement::class.java))
        assertFalse(isWrapperFor(String::class.java))
        assertEquals(preparedStatement, unwrap(JdbcPreparedStatement::class.java))
        assertFailsWith<SQLException> {
            preparedStatement.unwrap(String::class.java)
        }
    }

    @Test
    fun getSql() {
        assertEquals("SELECT * FROM users WHERE id = ? AND name = ?", preparedStatement.sql)
    }

    @Test
    fun parameterIndexValidation() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        preparedStatement.apply {
            setInt(1, 42)
            setString(2, "test")
        }.executeQuery()
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun repeatExecute() {
        val mockStatement = mock<ISQLStatement> {
            whenever(it.executeUpdateDelete()) doReturn 1
        }
        whenever(database.compileStatement(any<String>(), any<Array<Any?>>())) doReturn mockStatement
        val resultOne = preparedStatement.apply {
            setInt(1, 1)
            setString(2, "first")
        }.executeUpdate()
        val resultTwo = preparedStatement.apply {
            setInt(1, 2)
            setString(2, "second")
        }.executeUpdate()
        assertEquals(1, resultOne)
        assertEquals(1, resultTwo)
        verify(database, times(2)).compileStatement(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setAllBasicTypes() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(
            connection,
            database,
            "SELECT * FROM test WHERE a=? AND b=? AND c=? AND d=? AND e=? AND f=?"
        ).run {
            setBoolean(1, true)
            setByte(2, 42.toByte())
            setShort(3, 100.toShort())
            setLong(4, 1000L)
            setFloat(5, 1.5f)
            setDouble(6, 2.5)
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setBinaryData() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(
            connection,
            database,
            "SELECT * FROM test WHERE data=?"
        ).run {
            setBytes(1, byteArrayOf(1, 2, 3, 4))
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setStreams() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(
            connection,
            database,
            "SELECT * FROM test WHERE a=? AND b=?"
        ).run {
            "test".byteInputStream().use { asciiStream ->
                byteArrayOf(1, 2, 3).inputStream().use { binaryStream ->
                    setAsciiStream(1, asciiStream)
                    setBinaryStream(2, binaryStream)
                }
            }
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setCharacterStream() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(
            connection,
            database,
            "SELECT * FROM test WHERE text=?"
        ).run {
            "test data".reader().use {
                setCharacterStream(1, it)
            }
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setClob() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(
            connection,
            database,
            "SELECT * FROM test WHERE clob_data=?"
        ).run {
            setClob(1, mock<Clob> {
                whenever(it.getSubString(any(), any())) doReturn "clob data"
                whenever(it.length()) doReturn 9L
            })
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setObjectWithScaleAndTargetType() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(
            connection,
            database,
            "SELECT * FROM test WHERE value=?"
        ).run {
            setObject(1, 123.456, Types.DECIMAL, 2)
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun addBatchWithoutParameters() {
        whenever(database.batch(any<String>(), any<Sequence<Array<Any?>>>())) doReturn 2
        val results = JdbcPreparedStatement(
            connection,
            database,
            "UPDATE test SET value=? WHERE id=?"
        ).run {
            setInt(1, 100)
            setInt(2, 1)
            addBatch()
            setInt(1, 200)
            setInt(2, 2)
            addBatch()
            executeBatch()
        }
        assertEquals(2, results.size)
        verify(database).batch(any<String>(), any<Sequence<Array<Any?>>>())
    }

    @Test
    fun clearBatch() {
        whenever(database.batch(any<String>(), any<Sequence<Array<Any?>>>())) doReturn 0
        val results = JdbcPreparedStatement(
            connection,
            database,
            "UPDATE test SET value=? WHERE id=?"
        ).run {
            setInt(1, 100)
            setInt(2, 1)
            addBatch()
            clearBatch()
            executeBatch()
        }
        assertEquals(0, results.size)
    }

    @Test
    fun setNullWithTypeName() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(
            connection,
            database,
            "SELECT * FROM test WHERE value=?"
        ).run {
            setNull(1, Types.VARCHAR, "VARCHAR")
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun executeWithUnsetParameters() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(
            connection,
            database,
            "SELECT * FROM test WHERE a=? AND b=?"
        ).run {
            setInt(1, 42)
            execute()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setAsciiStreamWithLength() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE text=?").run {
            "test".byteInputStream().use {
                setAsciiStream(1, it, 4)
            }
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setAsciiStreamWithLongLength() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE text=?").run {
            "test".byteInputStream().use {
                setAsciiStream(1, it, 4L)
            }
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setBinaryStreamWithLength() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE data=?").run {
            byteArrayOf(1, 2, 3, 4).inputStream().use {
                setBinaryStream(1, it, 4)
            }
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setBinaryStreamWithLongLength() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE data=?").run {
            byteArrayOf(1, 2, 3, 4).inputStream().use {
                setBinaryStream(1, it, 4L)
            }
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setUnicodeStream() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE text=?").run {
            "test".byteInputStream().use {
                @Suppress("DEPRECATION")
                setUnicodeStream(1, it, 4)
            }
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setCharacterStreamWithLength() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE text=?").run {
            "test data".reader().use {
                setCharacterStream(1, it, 9)
            }
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setCharacterStreamWithLongLength() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE text=?").run {
            "test data".reader().use {
                setCharacterStream(1, it, 9L)
            }
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setDateWithCalendar() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE date=?").run {
            setDate(1, Date.valueOf("2026-02-04"), null)
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setTimeWithCalendar() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE time=?").run {
            setTime(1, Time.valueOf("10:30:45"), null)
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setTimestampWithCalendar() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE ts=?").run {
            setTimestamp(1, Timestamp.valueOf("2026-02-04 10:30:45"), null)
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setClobWithReader() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE clob=?").run {
            "clob content".reader().use {
                setClob(1, it)
            }
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setClobWithReaderAndLength() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE clob=?").run {
            "clob content".reader().use {
                setClob(1, it, 12L)
            }
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setClobNull() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE clob=?").run {
            setClob(1, null as Clob?)
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setClobReaderNull() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE clob=?").run {
            setClob(1, null as Reader?)
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setClobReaderWithLengthNull() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE clob=?").run {
            setClob(1, null as Reader?, 0L)
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setBigDecimal() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE amount=?").run {
            setBigDecimal(1, BigDecimal("123.45"))
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setBigDecimalNull() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(connection, database, "SELECT * FROM test WHERE amount=?").run {
            setBigDecimal(1, null)
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setNullValues() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(
            connection,
            database,
            "SELECT * FROM test WHERE a=? AND b=? AND c=? AND d=? AND e=?"
        ).run {
            setString(1, null)
            setBytes(2, null)
            setDate(3, null)
            setTime(4, null)
            setTimestamp(5, null)
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun setNullStreams() {
        whenever(database.query(any<String>(), any<Array<Any?>>())) doReturn cursor
        JdbcPreparedStatement(
            connection,
            database,
            "SELECT * FROM test WHERE a=? AND b=? AND c=?"
        ).run {
            setAsciiStream(1, null as InputStream?)
            setBinaryStream(2, null as InputStream?)
            setCharacterStream(3, null as Reader?)
            executeQuery()
        }
        verify(database).query(any<String>(), any<Array<Any?>>())
    }

    @Test
    fun executeBatchWithSelectStatement(): Unit = JdbcPreparedStatement(
        connection,
        database,
        "SELECT * FROM test WHERE id=?"
    ).run {
        setInt(1, 1)
        addBatch()
        assertFailsWith<SQLException> {
            executeBatch()
        }
    }

    @Test
    fun executeBatchWithException() {
        whenever(database.batch(any<String>(), any<Sequence<Array<out Any?>>>())) doThrow SQLException("Batch failed")
        JdbcPreparedStatement(connection, database, "UPDATE test SET value=? WHERE id=?").run {
            setInt(1, 100)
            setInt(2, 1)
            addBatch()
            assertFailsWith<SQLException> {
                executeBatch()
            }
        }
    }

    @Test
    fun largeBatch() {
        whenever(database.batch(any<String>(), any<Sequence<Array<Any?>>>())) doReturn 50
        val results = JdbcPreparedStatement(
            connection,
            database,
            "UPDATE test SET value=? WHERE id=?",
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY,
            ResultSet.CLOSE_CURSORS_AT_COMMIT
        ).run {
            for (i in 1..50) {
                setInt(1, i * 10)
                setInt(2, i)
                addBatch()
            }
            executeBatch()
        }
        assertEquals(50, results.size)
        verify(database).batch(any<String>(), any<Sequence<Array<Any?>>>())
    }

    @Test
    fun unsupportedBlobMethods(): Unit = JdbcPreparedStatement(
        connection,
        database,
        "SELECT * FROM test WHERE blob=?"
    ).run {
        assertFailsWith<SQLException> {
            setBlob(1, "data".byteInputStream(), 4L)
        }
        assertFailsWith<SQLException> {
            setBlob(1, "data".byteInputStream())
        }
    }

    @Test
    fun unsupportedNCharacterStreamWithLength(): Unit = JdbcPreparedStatement(
        connection,
        database,
        "SELECT * FROM test WHERE text=?"
    ).run {
        assertFailsWith<SQLException> {
            setNCharacterStream(1, "data".reader(), 4L)
        }
    }

    @Test
    fun unsupportedNClobMethods(): Unit = JdbcPreparedStatement(
        connection,
        database,
        "SELECT * FROM test WHERE clob=?"
    ).run {
        assertFailsWith<SQLException> {
            setNClob(1, "data".reader(), 4L)
        }
        assertFailsWith<SQLException> {
            setNClob(1, "data".reader())
        }
    }

    @Test
    fun parameterIndexOutOfRange(): Unit = JdbcPreparedStatement(
        connection,
        database,
        "SELECT * FROM test WHERE id=?"
    ).run {
        assertFailsWith<SQLException> {
            setInt(3, 42)
        }
        assertFailsWith<SQLException> {
            setString(0, "test")
        }
    }

    @Test
    fun clearParametersAfterClose(): Unit = preparedStatement.run {
        close()
        assertFailsWith<SQLException> {
            clearParameters()
        }
    }

    @Test
    fun addBatchAfterClose(): Unit = preparedStatement.run {
        close()
        assertFailsWith<SQLException> {
            addBatch()
        }
    }

    @Test
    fun clearBatchAfterClose(): Unit = preparedStatement.run {
        close()
        assertFailsWith<SQLException> {
            clearBatch()
        }
    }

    @Test
    fun executeBatchAfterClose(): Unit = preparedStatement.run {
        close()
        assertFailsWith<SQLException> {
            executeBatch()
        }
    }

    @Test
    fun executeUpdateAfterClose(): Unit = preparedStatement.run {
        close()
        assertFailsWith<SQLException> {
            executeUpdate()
        }
    }

    @Test
    fun executeAfterClose(): Unit = preparedStatement.run {
        close()
        assertFailsWith<SQLException> {
            execute()
        }
    }
}
