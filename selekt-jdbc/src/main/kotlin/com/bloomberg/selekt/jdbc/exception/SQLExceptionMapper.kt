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
import com.bloomberg.selekt.SQL_IO_ERROR_DIR_FSYNC
import com.bloomberg.selekt.SQL_IO_ERROR_DIR_FSTAT
import com.bloomberg.selekt.SQL_IO_ERROR_FSYNC
import com.bloomberg.selekt.SQL_IO_ERROR_GET_TEMP_PATH
import com.bloomberg.selekt.SQL_IO_ERROR_MMAP
import com.bloomberg.selekt.SQL_IO_ERROR_RDLOCK
import com.bloomberg.selekt.SQL_IO_ERROR_SEEK
import com.bloomberg.selekt.SQL_IO_ERROR_SHMLOCK
import com.bloomberg.selekt.SQL_IO_ERROR_SHMMAP
import com.bloomberg.selekt.SQL_IO_ERROR_SHMOPEN
import com.bloomberg.selekt.SQL_IO_ERROR_SHMSIZE
import com.bloomberg.selekt.SQL_IO_ERROR_SHORT_READ
import com.bloomberg.selekt.SQL_IO_ERROR_TRUNCATE
import com.bloomberg.selekt.SQL_IO_ERROR_WRITE
import com.bloomberg.selekt.SQL_IO_ERROR_LOCK
import com.bloomberg.selekt.SQL_IO_ERROR_NOMEM
import com.bloomberg.selekt.SQL_IO_ERROR_READ
import com.bloomberg.selekt.SQL_IO_ERROR_UNLOCK
import com.bloomberg.selekt.SQL_LOCKED
import com.bloomberg.selekt.SQL_LOCKED_SHARED_CACHE
import com.bloomberg.selekt.SQL_LOCKED_VTAB
import com.bloomberg.selekt.SQL_MISMATCH
import com.bloomberg.selekt.SQL_MISUSE
import com.bloomberg.selekt.SQL_NOMEM
import com.bloomberg.selekt.SQL_NOT_A_DATABASE
import com.bloomberg.selekt.SQL_NOT_FOUND
import com.bloomberg.selekt.SQL_NOTICE_RECOVER_ROLLBACK
import com.bloomberg.selekt.SQL_NOTICE_RECOVER_WAL
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
import com.bloomberg.selekt.SQLCode
import java.sql.SQLException
import java.sql.SQLDataException
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.SQLNonTransientConnectionException
import java.sql.SQLNonTransientException
import java.sql.SQLRecoverableException
import java.sql.SQLTimeoutException
import java.sql.SQLTransactionRollbackException
import java.sql.SQLTransientConnectionException
import java.sql.SQLTransientException

/**
 * @since 0.28.0
 */
internal object SQLExceptionMapper {
    private const val SQLSTATE_HY000 = "HY000"
    private const val SQLSTATE_53000 = "53000"
    private const val SQLSTATE_40001 = "40001"
    private const val SQLSTATE_08007 = "08007"

    @JvmStatic
    fun mapException(selektException: SQLException): SQLException = mapException(
        selektException.message ?: "Unknown error",
        extractSQLCode(selektException),
        extractExtendedSQLCode(selektException),
        selektException
    )

    @JvmStatic
    fun mapException(
        message: String,
        sqlCode: SQLCode,
        extendedSQLCode: SQLCode = -1,
        cause: Throwable? = null
    ): SQLException {
        val (exceptionClass, sqlState) = mapSQLCodeToExceptionClass(sqlCode, extendedSQLCode)
        val enhancedMessage = buildMessage(message, sqlCode, extendedSQLCode)
        return when (exceptionClass) {
            ExceptionType.DATA_EXCEPTION -> SQLDataException(enhancedMessage, sqlState, sqlCode, cause)
            ExceptionType.INTEGRITY_CONSTRAINT_VIOLATION -> SQLIntegrityConstraintViolationException(
                enhancedMessage,
                sqlState,
                sqlCode,
                cause
            )
            ExceptionType.NON_TRANSIENT_CONNECTION -> SQLNonTransientConnectionException(
                enhancedMessage,
                sqlState,
                sqlCode,
                cause
            )
            ExceptionType.NON_TRANSIENT -> SQLNonTransientException(enhancedMessage, sqlState, sqlCode, cause)
            ExceptionType.RECOVERABLE -> SQLRecoverableException(enhancedMessage, sqlState, sqlCode, cause)
            ExceptionType.TIMEOUT -> SQLTimeoutException(enhancedMessage, sqlState, sqlCode, cause)
            ExceptionType.TRANSACTION_ROLLBACK -> SQLTransactionRollbackException(enhancedMessage, sqlState, sqlCode, cause)
            ExceptionType.TRANSIENT_CONNECTION -> SQLTransientConnectionException(enhancedMessage, sqlState, sqlCode, cause)
            ExceptionType.TRANSIENT -> SQLTransientException(enhancedMessage, sqlState, sqlCode, cause)
            ExceptionType.GENERIC -> SQLException(enhancedMessage, sqlState, sqlCode, cause)
        }
    }

    private enum class ExceptionType {
        DATA_EXCEPTION,
        INTEGRITY_CONSTRAINT_VIOLATION,
        NON_TRANSIENT_CONNECTION,
        NON_TRANSIENT,
        RECOVERABLE,
        TIMEOUT,
        TRANSACTION_ROLLBACK,
        TRANSIENT_CONNECTION,
        TRANSIENT,
        GENERIC
    }

    private fun mapSQLCodeToExceptionClass(
        sqlCode: SQLCode,
        extendedSQLCode: SQLCode
    ): Pair<ExceptionType, String> = when (sqlCode) {
        SQL_CONSTRAINT -> ExceptionType.INTEGRITY_CONSTRAINT_VIOLATION to "23000"
        SQL_MISMATCH -> ExceptionType.DATA_EXCEPTION to "22000"
        SQL_TOO_BIG -> ExceptionType.DATA_EXCEPTION to "22001"
        SQL_RANGE -> ExceptionType.DATA_EXCEPTION to "22003"
        SQL_CANT_OPEN -> ExceptionType.NON_TRANSIENT_CONNECTION to "08001"
        SQL_NOT_A_DATABASE -> ExceptionType.NON_TRANSIENT_CONNECTION to SQLSTATE_08007
        SQL_CORRUPT -> ExceptionType.NON_TRANSIENT_CONNECTION to SQLSTATE_08007
        SQL_AUTH -> ExceptionType.NON_TRANSIENT_CONNECTION to "28000"
        SQL_BUSY -> when {
            isTimeoutRelated(extendedSQLCode) -> ExceptionType.TIMEOUT to "HYT00"
            else -> ExceptionType.TRANSIENT to SQLSTATE_40001
        }
        SQL_LOCKED, SQL_LOCKED_SHARED_CACHE, SQL_LOCKED_VTAB ->
            ExceptionType.TRANSIENT to SQLSTATE_40001

        SQL_ABORT, SQL_ABORT_ROLLBACK -> ExceptionType.TRANSACTION_ROLLBACK to "40000"
        SQL_IO_ERROR -> when (extendedSQLCode) {
            SQL_IO_ERROR_NOMEM -> ExceptionType.RECOVERABLE to SQLSTATE_53000
            SQL_IO_ERROR_ACCESS, SQL_IO_ERROR_LOCK, SQL_IO_ERROR_UNLOCK ->
                ExceptionType.TRANSIENT to SQLSTATE_HY000
            else -> ExceptionType.NON_TRANSIENT to SQLSTATE_HY000
        }
        SQL_NOMEM -> ExceptionType.RECOVERABLE to SQLSTATE_53000
        SQL_FULL -> ExceptionType.NON_TRANSIENT to "53100"
        SQL_READONLY -> ExceptionType.NON_TRANSIENT to "25006"
        SQL_MISUSE -> ExceptionType.NON_TRANSIENT to "HY010"
        SQL_NOT_FOUND -> ExceptionType.NON_TRANSIENT to "42000"
        SQL_ERROR -> ExceptionType.NON_TRANSIENT to SQLSTATE_HY000
        SQL_OK, SQL_ROW, SQL_DONE -> ExceptionType.GENERIC to "00000"
        else -> ExceptionType.GENERIC to SQLSTATE_HY000
    }

    private fun isTimeoutRelated(extendedSQLCode: SQLCode): Boolean {
        return when (extendedSQLCode) {
            SQL_IO_ERROR_BLOCKED -> true
            else -> false
        }
    }

    private fun buildMessage(message: String, sqlCode: SQLCode, extendedSQLCode: SQLCode): String {
        val codeDescription = getSQLCodeDescription(sqlCode)
        val extendedDescription = if (extendedSQLCode != -1) {
            getExtendedSQLCodeDescription(extendedSQLCode)
        } else {
            null
        }
        return buildString {
            append(message)
            if (codeDescription.isNotEmpty()) {
                append(" (").append(codeDescription)
                if (extendedDescription != null) {
                    append("; ").append(extendedDescription)
                }
                append(")")
            }
        }
    }

    private fun getSQLCodeDescription(sqlCode: SQLCode): String = when (sqlCode) {
        SQL_OK -> "SQLITE_OK"
        SQL_ERROR -> "SQLITE_ERROR"
        SQL_ABORT -> "SQLITE_ABORT"
        SQL_BUSY -> "SQLITE_BUSY"
        SQL_LOCKED -> "SQLITE_LOCKED"
        SQL_NOMEM -> "SQLITE_NOMEM"
        SQL_READONLY -> "SQLITE_READONLY"
        SQL_IO_ERROR -> "SQLITE_IOERR"
        SQL_CORRUPT -> "SQLITE_CORRUPT"
        SQL_NOT_FOUND -> "SQLITE_NOTFOUND"
        SQL_FULL -> "SQLITE_FULL"
        SQL_CANT_OPEN -> "SQLITE_CANTOPEN"
        SQL_TOO_BIG -> "SQLITE_TOOBIG"
        SQL_CONSTRAINT -> "SQLITE_CONSTRAINT"
        SQL_MISMATCH -> "SQLITE_MISMATCH"
        SQL_MISUSE -> "SQLITE_MISUSE"
        SQL_AUTH -> "SQLITE_AUTH"
        SQL_RANGE -> "SQLITE_RANGE"
        SQL_NOT_A_DATABASE -> "SQLITE_NOTADB"
        SQL_ROW -> "SQLITE_ROW"
        SQL_DONE -> "SQLITE_DONE"
        else -> "SQLITE_UNKNOWN($sqlCode)"
    }

    private fun getExtendedSQLCodeDescription(extendedSQLCode: SQLCode): String = when (extendedSQLCode) {
        SQL_ABORT_ROLLBACK -> "SQLITE_ABORT_ROLLBACK"
        SQL_IO_ERROR_ACCESS -> "SQLITE_IOERR_ACCESS"
        SQL_IO_ERROR_BLOCKED -> "SQLITE_IOERR_BLOCKED"
        SQL_IO_ERROR_CHECK_RESERVED_LOCK -> "SQLITE_IOERR_CHECKRESERVEDLOCK"
        SQL_IO_ERROR_CLOSE -> "SQLITE_IOERR_CLOSE"
        SQL_IO_ERROR_CONVPATH -> "SQLITE_IOERR_CONVPATH"
        SQL_IO_ERROR_DELETE -> "SQLITE_IOERR_DELETE"
        SQL_IO_ERROR_DELETE_NO_ENT -> "SQLITE_IOERR_DELETE_NOENT"
        SQL_IO_ERROR_DIR_CLOSE -> "SQLITE_IOERR_DIR_CLOSE"
        SQL_IO_ERROR_DIR_FSYNC -> "SQLITE_IOERR_DIR_FSYNC"
        SQL_IO_ERROR_DIR_FSTAT -> "SQLITE_IOERR_DIR_FSTAT"
        SQL_IO_ERROR_FSYNC -> "SQLITE_IOERR_FSYNC"
        SQL_IO_ERROR_GET_TEMP_PATH -> "SQLITE_IOERR_GETTEMPPATH"
        SQL_IO_ERROR_LOCK -> "SQLITE_IOERR_LOCK"
        SQL_IO_ERROR_MMAP -> "SQLITE_IOERR_MMAP"
        SQL_IO_ERROR_NOMEM -> "SQLITE_IOERR_NOMEM"
        SQL_IO_ERROR_RDLOCK -> "SQLITE_IOERR_RDLOCK"
        SQL_IO_ERROR_READ -> "SQLITE_IOERR_READ"
        SQL_IO_ERROR_SEEK -> "SQLITE_IOERR_SEEK"
        SQL_IO_ERROR_SHMLOCK -> "SQLITE_IOERR_SHMLOCK"
        SQL_IO_ERROR_SHMMAP -> "SQLITE_IOERR_SHMMAP"
        SQL_IO_ERROR_SHMOPEN -> "SQLITE_IOERR_SHMOPEN"
        SQL_IO_ERROR_SHMSIZE -> "SQLITE_IOERR_SHMSIZE"
        SQL_IO_ERROR_SHORT_READ -> "SQLITE_IOERR_SHORT_READ"
        SQL_IO_ERROR_TRUNCATE -> "SQLITE_IOERR_TRUNCATE"
        SQL_IO_ERROR_UNLOCK -> "SQLITE_IOERR_UNLOCK"
        SQL_IO_ERROR_WRITE -> "SQLITE_IOERR_WRITE"
        SQL_LOCKED_SHARED_CACHE -> "SQLITE_LOCKED_SHAREDCACHE"
        SQL_LOCKED_VTAB -> "SQLITE_LOCKED_VTAB"
        SQL_NOTICE_RECOVER_ROLLBACK -> "SQLITE_NOTICE_RECOVER_ROLLBACK"
        SQL_NOTICE_RECOVER_WAL -> "SQLITE_NOTICE_RECOVER_WAL"
        SQL_OK_LOAD_PERMANENTLY -> "SQLITE_OK_LOAD_PERMANENTLY"
        SQL_READONLY_CANT_INIT -> "SQLITE_READONLY_CANTINIT"
        SQL_READONLY_CANT_LOCK -> "SQLITE_READONLY_CANTLOCK"
        SQL_READONLY_DB_MOVED -> "SQLITE_READONLY_DBMOVED"
        SQL_READONLY_DIRECTORY -> "SQLITE_READONLY_DIRECTORY"
        SQL_READONLY_RECOVERY -> "SQLITE_READONLY_RECOVERY"
        SQL_READONLY_ROLLBACK -> "SQLITE_READONLY_ROLLBACK"
        SQL_WARNING_AUTOINDEX -> "SQLITE_WARNING_AUTOINDEX"
        else -> "SQLITE_UNKNOWN_EXTENDED($extendedSQLCode)"
    }

    private fun extractSQLCode(exception: SQLException): SQLCode {
        val message = exception.message ?: ""
        val codePattern = Regex("Code: (\\d+)")
        val match = codePattern.find(message)
        return match?.groups?.get(1)?.value?.toIntOrNull() ?: exception.errorCode
    }

    private fun extractExtendedSQLCode(exception: SQLException): SQLCode {
        val message = exception.message ?: ""
        val extendedPattern = Regex("Extended: (\\d+)")
        val match = extendedPattern.find(message)
        return match?.groups?.get(1)?.value?.toIntOrNull() ?: -1
    }
}
