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

package com.bloomberg.selekt.jdbc.exception

import com.bloomberg.selekt.SQL_ABORT
import com.bloomberg.selekt.SQL_ABORT_ROLLBACK
import com.bloomberg.selekt.SQL_AUTH
import com.bloomberg.selekt.SQL_BUSY
import com.bloomberg.selekt.SQL_CANT_OPEN
import com.bloomberg.selekt.SQL_CONSTRAINT
import com.bloomberg.selekt.SQL_CORRUPT
import com.bloomberg.selekt.SQL_DONE
import com.bloomberg.selekt.SQL_ERROR
import com.bloomberg.selekt.SQL_FULL
import com.bloomberg.selekt.SQL_IO_ERROR
import com.bloomberg.selekt.SQL_IO_ERROR_ACCESS
import com.bloomberg.selekt.SQL_IO_ERROR_BLOCKED
import com.bloomberg.selekt.SQL_IO_ERROR_CHECK_RESERVED_LOCK
import com.bloomberg.selekt.SQL_IO_ERROR_CLOSE
import com.bloomberg.selekt.SQL_IO_ERROR_CONVPATH
import com.bloomberg.selekt.SQL_IO_ERROR_DELETE
import com.bloomberg.selekt.SQL_IO_ERROR_DELETE_NO_ENT
import com.bloomberg.selekt.SQL_IO_ERROR_DIR_CLOSE
import com.bloomberg.selekt.SQL_IO_ERROR_DIR_FSTAT
import com.bloomberg.selekt.SQL_IO_ERROR_DIR_FSYNC
import com.bloomberg.selekt.SQL_IO_ERROR_FSYNC
import com.bloomberg.selekt.SQL_IO_ERROR_GET_TEMP_PATH
import com.bloomberg.selekt.SQL_IO_ERROR_LOCK
import com.bloomberg.selekt.SQL_IO_ERROR_MMAP
import com.bloomberg.selekt.SQL_IO_ERROR_NOMEM
import com.bloomberg.selekt.SQL_IO_ERROR_RDLOCK
import com.bloomberg.selekt.SQL_IO_ERROR_READ
import com.bloomberg.selekt.SQL_IO_ERROR_SEEK
import com.bloomberg.selekt.SQL_IO_ERROR_SHMLOCK
import com.bloomberg.selekt.SQL_IO_ERROR_SHMMAP
import com.bloomberg.selekt.SQL_IO_ERROR_SHMOPEN
import com.bloomberg.selekt.SQL_IO_ERROR_SHMSIZE
import com.bloomberg.selekt.SQL_IO_ERROR_SHORT_READ
import com.bloomberg.selekt.SQL_IO_ERROR_TRUNCATE
import com.bloomberg.selekt.SQL_IO_ERROR_UNLOCK
import com.bloomberg.selekt.SQL_IO_ERROR_WRITE
import com.bloomberg.selekt.SQL_LOCKED
import com.bloomberg.selekt.SQL_LOCKED_SHARED_CACHE
import com.bloomberg.selekt.SQL_LOCKED_VTAB
import com.bloomberg.selekt.SQL_MISMATCH
import com.bloomberg.selekt.SQL_MISUSE
import com.bloomberg.selekt.SQL_NOMEM
import com.bloomberg.selekt.SQL_NOTICE_RECOVER_ROLLBACK
import com.bloomberg.selekt.SQL_NOTICE_RECOVER_WAL
import com.bloomberg.selekt.SQL_NOT_A_DATABASE
import com.bloomberg.selekt.SQL_NOT_FOUND
import com.bloomberg.selekt.SQL_OK
import com.bloomberg.selekt.SQL_OK_LOAD_PERMANENTLY
import com.bloomberg.selekt.SQL_RANGE
import com.bloomberg.selekt.SQL_READONLY
import com.bloomberg.selekt.SQL_READONLY_CANT_INIT
import com.bloomberg.selekt.SQL_READONLY_CANT_LOCK
import com.bloomberg.selekt.SQL_READONLY_DB_MOVED
import com.bloomberg.selekt.SQL_READONLY_DIRECTORY
import com.bloomberg.selekt.SQL_READONLY_RECOVERY
import com.bloomberg.selekt.SQL_READONLY_ROLLBACK
import com.bloomberg.selekt.SQL_ROW
import com.bloomberg.selekt.SQL_TOO_BIG
import com.bloomberg.selekt.SQL_WARNING_AUTOINDEX
import java.sql.SQLDataException
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.SQLNonTransientConnectionException
import java.sql.SQLNonTransientException
import java.sql.SQLTimeoutException
import java.sql.SQLTransactionRollbackException
import java.sql.SQLTransientException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

internal class SQLExceptionMapperTest {
    @Test
    fun constraintViolationMapping(): Unit = SQLExceptionMapper.mapException(
        "Constraint violation",
        SQL_CONSTRAINT,
        -1
    ).run {
        assertTrue(this is SQLIntegrityConstraintViolationException)
        assertEquals("23000", sqlState)
        assertEquals(SQL_CONSTRAINT, errorCode)
        message!!.let { message ->
            assertTrue(message.contains("Constraint violation"))
            assertTrue(message.contains("SQLITE_CONSTRAINT"))
        }
    }

    @Test
    fun dataExceptionMapping(): Unit = SQLExceptionMapper.mapException("Type mismatch", SQL_MISMATCH, -1).run {
        assertTrue(this is SQLDataException)
        assertEquals("22000", sqlState)
        assertEquals(SQL_MISMATCH, errorCode)
    }

    @Test
    fun connectionExceptionMapping(): Unit = SQLExceptionMapper.mapException(
        "Cannot open database",
        SQL_CANT_OPEN,
        -1
    ).run {
        assertTrue(this is SQLNonTransientConnectionException)
        assertEquals("08001", sqlState)
        assertEquals(SQL_CANT_OPEN, errorCode)
    }

    @Test
    fun transientExceptionMapping(): Unit = SQLExceptionMapper.mapException("Database busy", SQL_BUSY, -1).run {
        assertTrue(this is SQLTransientException)
        assertEquals("40001", sqlState)
        assertEquals(SQL_BUSY, errorCode)
    }

    @Test
    fun timeoutExceptionMapping(): Unit = SQLExceptionMapper.mapException(
        "I/O blocked",
        SQL_BUSY,
        SQL_IO_ERROR_BLOCKED
    ).run {
        assertTrue(this is SQLTimeoutException)
        assertEquals("HYT00", sqlState)
        assertEquals(SQL_BUSY, errorCode)
    }

    @Test
    fun transactionRollbackMapping(): Unit = SQLExceptionMapper.mapException("Transaction aborted", SQL_ABORT, -1).run {
        assertTrue(this is SQLTransactionRollbackException)
        assertEquals("40000", sqlState)
        assertEquals(SQL_ABORT, errorCode)
    }

    @Test
    fun genericExceptionMapping(): Unit = SQLExceptionMapper.mapException("Unknown error", SQL_ERROR, -1).run {
        assertTrue(this is SQLNonTransientException)
        assertEquals("HY000", sqlState)
        assertEquals(SQL_ERROR, errorCode)
    }

    @Test
    fun exceptionFromSelektSQLException() {
        val originalMessage = "Code: $SQL_CONSTRAINT; Extended: -1; Message: Foreign key constraint failed; Context: INSERT"
        val selektException = SQLException(originalMessage)
        val mappedException = SQLExceptionMapper.mapException(selektException)
        assertTrue(mappedException is SQLIntegrityConstraintViolationException)
        assertEquals("23000", mappedException.sqlState)
    }

    @Test
    fun extendedCodeDescriptions(): Unit = SQLExceptionMapper.mapException(
        "I/O error",
        SQL_IO_ERROR,
        SQL_IO_ERROR_READ
    ).message!!.run {
        assertTrue(contains("SQLITE_IOERR"))
        assertTrue(contains("SQLITE_IOERR_READ"))
    }

    @Test
    fun nullCodeHandling(): Unit = SQLExceptionMapper.mapException("Generic error", -999, -1).run {
        assertEquals("HY000", sqlState)
        assertEquals(-999, errorCode)
    }

    @Test
    fun mapAllConstraintCodes() {
        SQLExceptionMapper.mapException("Constraint", SQL_CONSTRAINT).run {
            assertTrue(this is SQLIntegrityConstraintViolationException)
            assertEquals("23000", sqlState)
        }
    }

    @Test
    fun mapAllDataExceptionCodes() {
        SQLExceptionMapper.mapException("Mismatch", SQL_MISMATCH).run {
            assertTrue(this is SQLDataException)
            assertEquals("22000", sqlState)
        }
        SQLExceptionMapper.mapException("Too big", SQL_TOO_BIG).run {
            assertTrue(this is SQLDataException)
            assertEquals("22001", sqlState)
        }
        SQLExceptionMapper.mapException("Range", SQL_RANGE).run {
            assertTrue(this is SQLDataException)
            assertEquals("22003", sqlState)
        }
    }

    @Test
    fun mapAllConnectionExceptionCodes() {
        SQLExceptionMapper.mapException("Can't open", SQL_CANT_OPEN).run {
            assertTrue(this is SQLNonTransientConnectionException)
            assertEquals("08001", sqlState)
        }
        SQLExceptionMapper.mapException("Not a database", SQL_NOT_A_DATABASE).run {
            assertTrue(this is SQLNonTransientConnectionException)
            assertEquals("08007", sqlState)
        }
        SQLExceptionMapper.mapException("Corrupt", SQL_CORRUPT).run {
            assertTrue(this is SQLNonTransientConnectionException)
            assertEquals("08007", sqlState)
        }
        SQLExceptionMapper.mapException("Auth", SQL_AUTH).run {
            assertTrue(this is SQLNonTransientConnectionException)
            assertEquals("28000", sqlState)
        }
    }

    @Test
    fun mapTransientExceptionCodes() {
        SQLExceptionMapper.mapException("Busy", SQL_BUSY).run {
            assertTrue(this is SQLTransientException)
            assertEquals("40001", sqlState)
        }
        SQLExceptionMapper.mapException("Locked", SQL_LOCKED).run {
            assertTrue(this is SQLTransientException)
            assertEquals("40001", sqlState)
        }
        SQLExceptionMapper.mapException("Locked shared cache", SQL_LOCKED_SHARED_CACHE).run {
            assertTrue(this is SQLTransientException)
            assertEquals("40001", sqlState)
        }
        SQLExceptionMapper.mapException("Locked vtab", SQL_LOCKED_VTAB).run {
            assertTrue(this is SQLTransientException)
            assertEquals("40001", sqlState)
        }
    }

    @Test
    fun mapTransactionRollbackExceptionCodes() {
        SQLExceptionMapper.mapException("Abort", SQL_ABORT).run {
            assertTrue(this is SQLTransactionRollbackException)
            assertEquals("40000", sqlState)
        }
        SQLExceptionMapper.mapException("Abort rollback", SQL_ABORT_ROLLBACK).run {
            assertTrue(this is SQLTransactionRollbackException)
            assertEquals("40000", sqlState)
        }
    }

    @Test
    fun mapRecoverableExceptionCodes() {
        SQLExceptionMapper.mapException("No memory", SQL_NOMEM).run {
            assertTrue(this is java.sql.SQLRecoverableException)
            assertEquals("53000", sqlState)
        }
        SQLExceptionMapper.mapException("I/O error", SQL_IO_ERROR, SQL_IO_ERROR_NOMEM).run {
            assertTrue(this is java.sql.SQLRecoverableException)
            assertEquals("53000", sqlState)
        }
    }

    @Test
    fun mapIOErrorWithAccessExtendedCode() {
        SQLExceptionMapper.mapException("I/O error", SQL_IO_ERROR, SQL_IO_ERROR_ACCESS).run {
            assertTrue(this is SQLTransientException)
            assertEquals("HY000", sqlState)
        }
        SQLExceptionMapper.mapException("I/O error", SQL_IO_ERROR, SQL_IO_ERROR_LOCK).run {
            assertTrue(this is SQLTransientException)
            assertEquals("HY000", sqlState)
        }
        SQLExceptionMapper.mapException("I/O error", SQL_IO_ERROR, SQL_IO_ERROR_UNLOCK).run {
            assertTrue(this is SQLTransientException)
            assertEquals("HY000", sqlState)
        }
    }

    @Test
    fun mapIOErrorWithGenericExtendedCode() {
        SQLExceptionMapper.mapException("I/O error", SQL_IO_ERROR, SQL_IO_ERROR_READ).run {
            assertTrue(this is SQLNonTransientException)
            assertEquals("HY000", sqlState)
        }
    }

    @Test
    fun mapIOErrorNoExtendedCode() {
        SQLExceptionMapper.mapException("I/O error", SQL_IO_ERROR).run {
            assertTrue(this is SQLNonTransientException)
            assertEquals("HY000", sqlState)
        }
    }

    @Test
    fun mapNonTransientExceptionCodes() {
        SQLExceptionMapper.mapException("Full", SQL_FULL).run {
            assertTrue(this is SQLNonTransientException)
            assertEquals("53100", sqlState)
        }
        SQLExceptionMapper.mapException("Read only", SQL_READONLY).run {
            assertTrue(this is SQLNonTransientException)
            assertEquals("25006", sqlState)
        }
        SQLExceptionMapper.mapException("Misuse", SQL_MISUSE).run {
            assertTrue(this is SQLNonTransientException)
            assertEquals("HY010", sqlState)
        }
        SQLExceptionMapper.mapException("Not found", SQL_NOT_FOUND).run {
            assertTrue(this is SQLNonTransientException)
            assertEquals("42000", sqlState)
        }
        SQLExceptionMapper.mapException("Error", SQL_ERROR).run {
            assertTrue(this is SQLNonTransientException)
            assertEquals("HY000", sqlState)
        }
    }

    @Test
    fun mapSuccessCodes() {
        SQLExceptionMapper.mapException("OK", SQL_OK).run {
            assertEquals("00000", sqlState)
            assertEquals(SQL_OK, errorCode)
        }
        SQLExceptionMapper.mapException("Row", SQL_ROW).run {
            assertEquals("00000", sqlState)
            assertEquals(SQL_ROW, errorCode)
        }
        SQLExceptionMapper.mapException("Done", SQL_DONE).run {
            assertEquals("00000", sqlState)
            assertEquals(SQL_DONE, errorCode)
        }
    }

    @Suppress("Detekt.CyclomaticComplexMethod", "Detekt.LongMethod")
    @Test
    fun extendedCodeDescriptionsComprehensive() {
        SQLExceptionMapper.mapException("Abort", SQL_ABORT, SQL_ABORT_ROLLBACK).message!!.run {
            assertTrue(contains("SQLITE_ABORT_ROLLBACK"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_CHECK_RESERVED_LOCK).message!!.run {
            assertTrue(contains("SQLITE_IOERR_CHECKRESERVEDLOCK"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_CLOSE).message!!.run {
            assertTrue(contains("SQLITE_IOERR_CLOSE"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_CONVPATH).message!!.run {
            assertTrue(contains("SQLITE_IOERR_CONVPATH"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_DELETE).message!!.run {
            assertTrue(contains("SQLITE_IOERR_DELETE"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_DELETE_NO_ENT).message!!.run {
            assertTrue(contains("SQLITE_IOERR_DELETE_NOENT"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_DIR_CLOSE).message!!.run {
            assertTrue(contains("SQLITE_IOERR_DIR_CLOSE"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_DIR_FSYNC).message!!.run {
            assertTrue(contains("SQLITE_IOERR_DIR_FSYNC"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_DIR_FSTAT).message!!.run {
            assertTrue(contains("SQLITE_IOERR_DIR_FSTAT"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_FSYNC).message!!.run {
            assertTrue(contains("SQLITE_IOERR_FSYNC"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_GET_TEMP_PATH).message!!.run {
            assertTrue(contains("SQLITE_IOERR_GETTEMPPATH"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_MMAP).message!!.run {
            assertTrue(contains("SQLITE_IOERR_MMAP"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_RDLOCK).message!!.run {
            assertTrue(contains("SQLITE_IOERR_RDLOCK"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_SEEK).message!!.run {
            assertTrue(contains("SQLITE_IOERR_SEEK"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_SHMLOCK).message!!.run {
            assertTrue(contains("SQLITE_IOERR_SHMLOCK"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_SHMMAP).message!!.run {
            assertTrue(contains("SQLITE_IOERR_SHMMAP"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_SHMOPEN).message!!.run {
            assertTrue(contains("SQLITE_IOERR_SHMOPEN"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_SHMSIZE).message!!.run {
            assertTrue(contains("SQLITE_IOERR_SHMSIZE"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_SHORT_READ).message!!.run {
            assertTrue(contains("SQLITE_IOERR_SHORT_READ"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_TRUNCATE).message!!.run {
            assertTrue(contains("SQLITE_IOERR_TRUNCATE"))
        }
        SQLExceptionMapper.mapException("I/O", SQL_IO_ERROR, SQL_IO_ERROR_WRITE).message!!.run {
            assertTrue(contains("SQLITE_IOERR_WRITE"))
        }
    }

    @Test
    fun readonlyExtendedCodes() {
        SQLExceptionMapper.mapException("RO", SQL_READONLY, SQL_READONLY_CANT_INIT).message!!.run {
            assertTrue(contains("SQLITE_READONLY_CANTINIT"))
        }
        SQLExceptionMapper.mapException("RO", SQL_READONLY, SQL_READONLY_CANT_LOCK).message!!.run {
            assertTrue(contains("SQLITE_READONLY_CANTLOCK"))
        }
        SQLExceptionMapper.mapException("RO", SQL_READONLY, SQL_READONLY_DB_MOVED).message!!.run {
            assertTrue(contains("SQLITE_READONLY_DBMOVED"))
        }
        SQLExceptionMapper.mapException("RO", SQL_READONLY, SQL_READONLY_DIRECTORY).message!!.run {
            assertTrue(contains("SQLITE_READONLY_DIRECTORY"))
        }
        SQLExceptionMapper.mapException("RO", SQL_READONLY, SQL_READONLY_RECOVERY).message!!.run {
            assertTrue(contains("SQLITE_READONLY_RECOVERY"))
        }
        SQLExceptionMapper.mapException("RO", SQL_READONLY, SQL_READONLY_ROLLBACK).message!!.run {
            assertTrue(contains("SQLITE_READONLY_ROLLBACK"))
        }
    }

    @Test
    fun noticeAndWarningExtendedCodes() {
        SQLExceptionMapper.mapException("Notice", SQL_OK, SQL_NOTICE_RECOVER_ROLLBACK).message!!.run {
            assertTrue(contains("SQLITE_NOTICE_RECOVER_ROLLBACK"))
        }
        SQLExceptionMapper.mapException("Notice", SQL_OK, SQL_NOTICE_RECOVER_WAL).message!!.run {
            assertTrue(contains("SQLITE_NOTICE_RECOVER_WAL"))
        }
        SQLExceptionMapper.mapException("Warning", SQL_OK, SQL_WARNING_AUTOINDEX).message!!.run {
            assertTrue(contains("SQLITE_WARNING_AUTOINDEX"))
        }
        SQLExceptionMapper.mapException("OK", SQL_OK, SQL_OK_LOAD_PERMANENTLY).message!!.run {
            assertTrue(contains("SQLITE_OK_LOAD_PERMANENTLY"))
        }
    }

    @Test
    fun lockedExtendedCodeDescriptions() {
        SQLExceptionMapper.mapException("Locked", SQL_LOCKED, SQL_LOCKED_SHARED_CACHE).message!!.run {
            assertTrue(contains("SQLITE_LOCKED_SHAREDCACHE"))
        }
        SQLExceptionMapper.mapException("Locked", SQL_LOCKED, SQL_LOCKED_VTAB).message!!.run {
            assertTrue(contains("SQLITE_LOCKED_VTAB"))
        }
    }

    @Test
    fun unknownExtendedCode() {
        SQLExceptionMapper.mapException("Unknown", SQL_ERROR, -5678).message!!.run {
            assertTrue(contains("SQLITE_UNKNOWN_EXTENDED(-5678)"))
        }
    }

    @Test
    fun unknownPrimaryCode() {
        SQLExceptionMapper.mapException("Unknown", -1234).message!!.run {
            assertTrue(contains("SQLITE_UNKNOWN(-1234)"))
        }
    }

    @Test
    fun messageWithoutExtendedCode() {
        SQLExceptionMapper.mapException("Test error", SQL_ERROR).message!!.run {
            listOf(
                "Test error",
                "SQLITE_ERROR"
            ).forEach {
                assertTrue(contains(it))
            }
            assertFalse(contains(";"))
        }
    }

    @Test
    fun messageWithExtendedCode() {
        SQLExceptionMapper.mapException("Test error", SQL_IO_ERROR, SQL_IO_ERROR_READ).message!!.run {
            listOf(
                "Test error",
                "SQLITE_IOERR",
                ";",
                "SQLITE_IOERR_READ"
            ).forEach {
                assertTrue(contains(it))
            }
        }
    }

    @Test
    fun exceptionWithCause() {
        SQLExceptionMapper.mapException("Error", SQL_ERROR, -1, RuntimeException("Original cause")).run {
            assertEquals(cause, this.cause)
        }
    }

    @Test
    fun extractSQLCodeFromMessage() {
        val mapped = SQLExceptionMapper.mapException(
            SQLException("Code: 19; Extended: -1; Message: UNIQUE constraint failed")
        )
        assertTrue(mapped is SQLIntegrityConstraintViolationException)
        assertEquals(19, mapped.errorCode)
    }

    @Test
    fun extractExtendedSQLCodeFromMessage() {
        val mapped = SQLExceptionMapper.mapException(
            SQLException("Code: $SQL_BUSY; Extended: $SQL_IO_ERROR_BLOCKED; Message: I/O error blocked")
        )
        assertTrue(mapped is SQLTimeoutException)
        assertTrue(mapped.message!!.contains("SQLITE_IOERR_BLOCKED"))
    }

    @Test
    fun extractNoCodeFromMessage() {
        assertEquals(-1, SQLExceptionMapper.mapException(SQLException("No code in message", "HY000", -1)).errorCode)
    }
}
