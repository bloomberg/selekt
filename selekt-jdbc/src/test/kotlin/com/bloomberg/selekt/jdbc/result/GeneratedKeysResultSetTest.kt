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

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.sql.Statement
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class GeneratedKeysResultSetTest {
    private val statement = mock<Statement>()

    @Test
    fun nextMovesToFirstRowThenReturnsFalse() {
        GeneratedKeysResultSet(42L, statement).run {
            assertTrue(next())
            assertFalse(next())
            assertFalse(next())
        }
    }

    @Test
    fun closeMarksResultSetAsClosed() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFalse(isClosed)
            close()
            assertTrue(isClosed)
        }
    }

    @Test
    fun operationsOnClosedResultSetThrowException() {
        GeneratedKeysResultSet(42L, statement).run {
            close()
            assertFailsWith<SQLException> { next() }
            assertFailsWith<SQLException> { getString(1) }
            assertFailsWith<SQLException> { getLong(1) }
            assertFailsWith<SQLException> { beforeFirst() }
            assertFailsWith<SQLException> { first() }
        }
    }

    @Test
    fun wasNullAlwaysReturnsFalse() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFalse(wasNull())
        }
    }

    @Test
    fun getStringReturnsGeneratedKeyAsString() {
        GeneratedKeysResultSet(123L, statement).run {
            next()
            assertEquals("123", getString(1))
            assertEquals("123", getString("any_label"))
        }
    }

    @Test
    fun getStringBeforeFirstRowThrowsException() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFailsWith<SQLException> { getString(1) }
        }
    }

    @Test
    fun getStringWithInvalidColumnIndexThrowsException() {
        GeneratedKeysResultSet(42L, statement).run {
            next()
            assertFailsWith<SQLException> { getString(0) }
            assertFailsWith<SQLException> { getString(2) }
        }
    }

    @Test
    fun getBooleanReturnsTrueForNonZero() {
        GeneratedKeysResultSet(42L, statement).run {
            next()
            assertTrue(getBoolean(1))
            assertTrue(getBoolean("any_label"))
        }
    }

    @Test
    fun getBooleanReturnsFalseForZero() {
        GeneratedKeysResultSet(0L, statement).run {
            next()
            assertFalse(getBoolean(1))
        }
    }

    @Test
    fun getByteReturnsGeneratedKeyAsByte() {
        GeneratedKeysResultSet(42L, statement).run {
            next()
            assertEquals(42.toByte(), getByte(1))
            assertEquals(42.toByte(), getByte("any_label"))
        }
    }

    @Test
    fun getShortReturnsGeneratedKeyAsShort() {
        GeneratedKeysResultSet(1000L, statement).run {
            next()
            assertEquals(1000.toShort(), getShort(1))
            assertEquals(1000.toShort(), getShort("any_label"))
        }
    }

    @Test
    fun getIntReturnsGeneratedKeyAsInt() {
        GeneratedKeysResultSet(123_456L, statement).run {
            next()
            assertEquals(123_456, getInt(1))
            assertEquals(123_456, getInt("any_label"))
        }
    }

    @Test
    fun getLongReturnsGeneratedKey() {
        GeneratedKeysResultSet(9_876_543_210L, statement).run {
            next()
            assertEquals(9_876_543_210L, getLong(1))
            assertEquals(9_876_543_210L, getLong("any_label"))
        }
    }

    @Test
    fun getLongBeforeFirstRowThrowsException() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFailsWith<SQLException> { getLong(1) }
        }
    }

    @Test
    fun getLongWithInvalidColumnIndexThrowsException() {
        GeneratedKeysResultSet(42L, statement).run {
            next()
            assertFailsWith<SQLException> { getLong(0) }
            assertFailsWith<SQLException> { getLong(2) }
        }
    }

    @Test
    fun getFloatReturnsGeneratedKeyAsFloat() {
        GeneratedKeysResultSet(42L, statement).run {
            next()
            assertEquals(42.0f, getFloat(1))
            assertEquals(42.0f, getFloat("any_label"))
        }
    }

    @Test
    fun getDoubleReturnsGeneratedKeyAsDouble() {
        GeneratedKeysResultSet(42L, statement).run {
            next()
            assertEquals(42.0, getDouble(1))
            assertEquals(42.0, getDouble("any_label"))
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun getBigDecimalWithScaleReturnsGeneratedKey() {
        GeneratedKeysResultSet(42L, statement).run {
            next()
            assertEquals(BigDecimal(42), getBigDecimal(1, 2))
            assertEquals(BigDecimal(42), getBigDecimal("any_label", 2))
        }
    }

    @Test
    fun getBigDecimalReturnsGeneratedKey() {
        GeneratedKeysResultSet(12345L, statement).run {
            next()
            assertEquals(BigDecimal(12345), getBigDecimal(1))
            assertEquals(BigDecimal(12345), getBigDecimal("any_label"))
        }
    }

    @Test
    fun getBytesReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getBytes(1))
            assertNull(getBytes("any_label"))
        }
    }

    @Test
    fun getDateReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getDate(1))
            assertNull(getDate("any_label"))
            assertNull(getDate(1, null))
            assertNull(getDate("any_label", null))
        }
    }

    @Test
    fun getTimeReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getTime(1))
            assertNull(getTime("any_label"))
            assertNull(getTime(1, null))
            assertNull(getTime("any_label", null))
        }
    }

    @Test
    fun getTimestampReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getTimestamp(1))
            assertNull(getTimestamp("any_label"))
            assertNull(getTimestamp(1, null))
            assertNull(getTimestamp("any_label", null))
        }
    }

    @Test
    fun getAsciiStreamReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getAsciiStream(1))
            assertNull(getAsciiStream("any_label"))
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun getUnicodeStreamReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getUnicodeStream(1))
            assertNull(getUnicodeStream("any_label"))
        }
    }

    @Test
    fun getBinaryStreamReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getBinaryStream(1))
            assertNull(getBinaryStream("any_label"))
        }
    }

    @Test
    fun getWarningsReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(warnings)
        }
    }

    @Test
    fun clearWarningsDoesNotThrow() {
        GeneratedKeysResultSet(42L, statement).run {
            clearWarnings()
        }
    }

    @Test
    fun getCursorNameThrowsUnsupported() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFailsWith<SQLFeatureNotSupportedException> { cursorName }
        }
    }

    @Test
    fun getMetaDataThrowsUnsupported() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFailsWith<SQLFeatureNotSupportedException> { metaData }
        }
    }

    @Test
    fun getObjectReturnsGeneratedKey() {
        GeneratedKeysResultSet(42L, statement).run {
            next()
            assertEquals(42L, getObject(1))
            assertEquals(42L, getObject("any_label"))
            assertEquals(42L, getObject(1, null))
            assertEquals(42L, getObject("any_label", null))
        }
    }

    @Test
    fun getObjectWithTypeReturnsGeneratedKey() {
        GeneratedKeysResultSet(42L, statement).run {
            next()
            assertEquals(42L, getObject(1, Long::class.java))
            assertEquals(42L, getObject("any_label", Long::class.java))
        }
    }

    @Test
    fun findColumnReturnsOne() {
        GeneratedKeysResultSet(42L, statement).run {
            assertEquals(1, findColumn("any_label"))
        }
    }

    @Test
    fun getCharacterStreamReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getCharacterStream(1))
            assertNull(getCharacterStream("any_label"))
        }
    }

    @Test
    fun isBeforeFirstReturnsTrueInitially() {
        GeneratedKeysResultSet(42L, statement).run {
            assertTrue(isBeforeFirst)
            next()
            assertFalse(isBeforeFirst)
        }
    }

    @Test
    fun isAfterLastReturnsTrueAfterLastRow() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFalse(isAfterLast)
            next()
            assertFalse(isAfterLast)
            afterLast()
            assertTrue(isAfterLast)
        }
    }

    @Test
    fun isFirstReturnsTrueOnFirstRow() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFalse(isFirst)
            next()
            assertTrue(isFirst)
            afterLast()
            assertFalse(isFirst)
        }
    }

    @Test
    fun isLastReturnsTrueOnFirstRow() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFalse(isLast)
            next()
            assertTrue(isLast)
        }
    }

    @Test
    fun beforeFirstResetsPosition() {
        GeneratedKeysResultSet(42L, statement).run {
            next()
            assertFalse(isBeforeFirst)
            beforeFirst()
            assertTrue(isBeforeFirst)
        }
    }

    @Test
    fun afterLastMovesPositionAfterEnd() {
        GeneratedKeysResultSet(42L, statement).run {
            afterLast()
            assertTrue(isAfterLast)
        }
    }

    @Test
    fun firstMovesToFirstRow() {
        GeneratedKeysResultSet(42L, statement).run {
            assertTrue(first())
            assertTrue(isFirst)
        }
    }

    @Test
    fun lastMovesToFirstRow() {
        GeneratedKeysResultSet(42L, statement).run {
            assertTrue(last())
            assertTrue(isFirst)
        }
    }

    @Test
    fun getRowReturnsOneOnFirstRow() {
        GeneratedKeysResultSet(42L, statement).run {
            assertEquals(0, row)
            next()
            assertEquals(1, row)
            afterLast()
            assertEquals(0, row)
        }
    }

    @Test
    fun absoluteMovesToSpecifiedRow() {
        GeneratedKeysResultSet(42L, statement).run {
            assertTrue(absolute(1))
            assertTrue(isFirst)
            assertFalse(absolute(2))
            assertTrue(isAfterLast)
        }
    }

    @Test
    fun relativeMovesRelativeToCurrentPosition() {
        GeneratedKeysResultSet(42L, statement).run {
            assertTrue(relative(1))
            assertEquals(1, row)
            assertFalse(relative(1))
            assertEquals(0, row)
            assertTrue(isAfterLast)
        }
    }

    @Test
    fun previousMovesToPreviousRow() {
        GeneratedKeysResultSet(42L, statement).run {
            next()
            assertTrue(isFirst)
            assertFalse(previous())
            assertTrue(isBeforeFirst)
            assertTrue(next())
            assertTrue(isFirst)
        }
    }

    @Test
    fun setFetchDirectionOnlyAcceptsForward() {
        GeneratedKeysResultSet(42L, statement).run {
            fetchDirection = ResultSet.FETCH_FORWARD
            assertFailsWith<SQLFeatureNotSupportedException> {
                fetchDirection = ResultSet.FETCH_REVERSE
            }
        }
    }

    @Test
    fun getFetchDirectionReturnsForward() {
        GeneratedKeysResultSet(42L, statement).run {
            assertEquals(ResultSet.FETCH_FORWARD, fetchDirection)
        }
    }

    @Test
    fun setFetchSizeDoesNotThrow() {
        GeneratedKeysResultSet(42L, statement).run {
            fetchSize = 100
        }
    }

    @Test
    fun getFetchSizeReturnsOne() {
        GeneratedKeysResultSet(42L, statement).run {
            assertEquals(1, fetchSize)
        }
    }

    @Test
    fun getTypeReturnsForwardOnly() {
        GeneratedKeysResultSet(42L, statement).run {
            assertEquals(ResultSet.TYPE_FORWARD_ONLY, type)
        }
    }

    @Test
    fun getConcurrencyReturnsReadOnly() {
        GeneratedKeysResultSet(42L, statement).run {
            assertEquals(ResultSet.CONCUR_READ_ONLY, concurrency)
        }
    }

    @Test
    fun rowUpdatedReturnsFalse() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFalse(rowUpdated())
        }
    }

    @Test
    fun rowInsertedReturnsFalse() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFalse(rowInserted())
        }
    }

    @Test
    fun rowDeletedReturnsFalse() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFalse(rowDeleted())
        }
    }

    @Test
    fun updateMethodsThrowUnsupported() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFailsWith<SQLFeatureNotSupportedException> { updateNull(1) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateBoolean(1, true) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateByte(1, 0) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateShort(1, 0) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateInt(1, 0) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateLong(1, 0L) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateFloat(1, 0f) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateDouble(1, 0.0) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateBigDecimal(1, null) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateString(1, null) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateBytes(1, null) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateDate(1, null) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateTime(1, null) }
            assertFailsWith<SQLFeatureNotSupportedException> { updateTimestamp(1, null) }
        }
    }

    @Test
    fun rowOperationsThrowUnsupported() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFailsWith<SQLFeatureNotSupportedException> { insertRow() }
            assertFailsWith<SQLFeatureNotSupportedException> { updateRow() }
            assertFailsWith<SQLFeatureNotSupportedException> { deleteRow() }
            assertFailsWith<SQLFeatureNotSupportedException> { refreshRow() }
            assertFailsWith<SQLFeatureNotSupportedException> { cancelRowUpdates() }
            assertFailsWith<SQLFeatureNotSupportedException> { moveToInsertRow() }
            assertFailsWith<SQLFeatureNotSupportedException> { moveToCurrentRow() }
        }
    }

    @Test
    fun getStatementReturnsProvidedStatement() {
        GeneratedKeysResultSet(42L, statement).run {
            assertEquals(statement, this.statement)
        }
    }

    @Test
    fun getLobMethodsReturnNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getRef(1))
            assertNull(getBlob(1))
            assertNull(getClob(1))
            assertNull(getArray(1))
            assertNull(getNClob(1))
        }
    }

    @Test
    fun getURLReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getURL(1))
            assertNull(getURL("any_label"))
        }
    }

    @Test
    fun getRowIdReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getRowId(1))
            assertNull(getRowId("any_label"))
        }
    }

    @Test
    fun getHoldabilityReturnsCloseCursorsAtCommit() {
        GeneratedKeysResultSet(42L, statement).run {
            assertEquals(ResultSet.CLOSE_CURSORS_AT_COMMIT, holdability)
        }
    }

    @Test
    fun getNStringReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getNString(1))
            assertNull(getNString("any_label"))
        }
    }

    @Test
    fun getNCharacterStreamReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getNCharacterStream(1))
            assertNull(getNCharacterStream("any_label"))
        }
    }

    @Test
    fun getSQLXMLReturnsNull() {
        GeneratedKeysResultSet(42L, statement).run {
            assertNull(getSQLXML(1))
            assertNull(getSQLXML("any_label"))
        }
    }

    @Test
    fun unwrapReturnsThisForCompatibleType() {
        GeneratedKeysResultSet(42L, statement).run {
            assertEquals(this, unwrap(GeneratedKeysResultSet::class.java))
            assertEquals(this, unwrap(ResultSet::class.java))
        }
    }

    @Test
    fun unwrapThrowsForIncompatibleType() {
        GeneratedKeysResultSet(42L, statement).run {
            assertFailsWith<SQLException> {
                unwrap(String::class.java)
            }
        }
    }

    @Test
    fun isWrapperForReturnsTrueForCompatibleType() {
        GeneratedKeysResultSet(42L, statement).run {
            assertTrue(isWrapperFor(GeneratedKeysResultSet::class.java))
            assertTrue(isWrapperFor(ResultSet::class.java))
            assertFalse(isWrapperFor(String::class.java))
        }
    }
}
