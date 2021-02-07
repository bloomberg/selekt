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

package com.bloomberg.selekt

import javax.annotation.concurrent.ThreadSafe

internal enum class SQLStatementType(
    @JvmField
    val isPredictedWrite: Boolean = true,
    @JvmField
    val isTransactional: Boolean = false,
    @JvmField
    val aborts: Boolean = false,
    @JvmField
    val commits: Boolean = false,
    @JvmField
    val begins: Boolean = false
) {
    ABORT(aborts = true, isTransactional = true),
    ATTACH,
    BEGIN(begins = true, isTransactional = true),
    COMMIT(commits = true, isTransactional = true),
    DDL,
    OTHER,
    PRAGMA,
    SELECT(false),
    UNPREPARED,
    UPDATE
}

private const val SUFFICIENT_SQL_PREFIX_LENGTH = 3

internal fun CharSequence.resolvedSqlStatementType() = trimStart(Char::isWhitespace).run {
    if (length < SUFFICIENT_SQL_PREFIX_LENGTH) {
        return SQLStatementType.OTHER
    }
    when (this[0].toUpperCase()) {
        'S' -> when (this[1].toUpperCase()) {
            'E' -> SQLStatementType.SELECT
            else -> SQLStatementType.OTHER // SAVEPOINT, ...
        }
        'I', 'U' -> SQLStatementType.UPDATE
        'D' -> when (this[2].toUpperCase()) {
            'L' -> SQLStatementType.UPDATE // DELETE
            'O' -> SQLStatementType.DDL
            'T' -> SQLStatementType.UNPREPARED // DETACH
            else -> SQLStatementType.OTHER
        }
        'R' -> when (this[2].toUpperCase()) {
            'L' -> SQLStatementType.ABORT // TODO what about to savepoint, "ROLLBACK TO ..."? In that case, map to OTHER.
            'P' -> SQLStatementType.UPDATE // REPLACE
            else -> SQLStatementType.OTHER
        }
        'B' -> SQLStatementType.BEGIN
        'C' -> when (this[1].toUpperCase()) {
            'O' -> SQLStatementType.COMMIT
            'R' -> SQLStatementType.DDL // CREATE
            else -> SQLStatementType.OTHER
        }
        'E' -> when (this[1].toUpperCase()) {
            'N' -> SQLStatementType.COMMIT
            else -> SQLStatementType.OTHER
        }
        'P' -> SQLStatementType.PRAGMA
        'A' -> when (this[1].toUpperCase()) {
            'L' -> SQLStatementType.DDL // ALTER
            'T' -> SQLStatementType.ATTACH
            'N' -> SQLStatementType.UNPREPARED
            else -> SQLStatementType.OTHER
        }
        else -> SQLStatementType.OTHER
    }
}

@ThreadSafe
internal class BatchSQLStatement private constructor(
    private val session: ThreadLocalisedSession,
    private val sql: String,
    private val args: Sequence<Array<out Any?>>
) {
    companion object {
        fun compile(
            session: ThreadLocalisedSession,
            sql: String,
            bindArgs: Sequence<Array<out Any?>>
        ): BatchSQLStatement {
            require(SQLStatementType.UPDATE == sql.resolvedSqlStatementType()) {
                "Only batched updates are permitted."
            }
            return session.execute(true, sql) {
                it.prepare(sql)
            }.run {
                check(!isReadOnly) { "Prepared statement for batching must not be read-only." }
                @Suppress("UNCHECKED_CAST")
                BatchSQLStatement(
                    session,
                    sql,
                    bindArgs
                )
            }
        }
    }

    internal fun execute() = session.execute(true, sql) {
        it.executeForChangedRowCount(sql, args)
    }
}

@ThreadSafe
internal class SQLStatement private constructor(
    private val session: ThreadLocalisedSession,
    private val sql: String,
    private val statementType: SQLStatementType,
    private val args: Array<Any?>,
    private val asWrite: Boolean
) : ISQLStatement {
    companion object {
        fun execute(
            session: ThreadLocalisedSession,
            sql: String,
            statementType: SQLStatementType,
            bindArgs: Array<out Any?>
        ) = session.execute(statementType.isPredictedWrite, sql) {
            it.execute(sql, bindArgs)
        }

        fun executeForInt(
            session: ThreadLocalisedSession,
            sql: String,
            statementType: SQLStatementType,
            bindArgs: Array<out Any?>
        ) = session.execute(statementType.isPredictedWrite, sql) {
            it.executeForInt(sql, bindArgs)
        }

        fun executeForString(
            session: ThreadLocalisedSession,
            sql: String,
            statementType: SQLStatementType,
            bindArgs: Array<out Any?>
        ) = session.execute(statementType.isPredictedWrite, sql) {
            it.executeForString(sql, bindArgs)
        }

        fun executeInsert(
            session: ThreadLocalisedSession,
            sql: String,
            bindArgs: Array<out Any?>
        ) = session.execute(true, sql) {
            it.executeForLastInsertedRowId(sql, bindArgs)
        }

        fun executeUpdateDelete(
            session: ThreadLocalisedSession,
            sql: String,
            bindArgs: Array<out Any?>
        ) = session.execute(true, sql) {
            it.executeForChangedRowCount(sql, bindArgs)
        }

        fun compile(
            session: ThreadLocalisedSession,
            sql: String,
            statementType: SQLStatementType,
            bindArgs: Array<out Any?>?
        ): SQLStatement {
            return session.execute(statementType.isPredictedWrite, sql) {
                it.prepare(sql)
            }.run {
                @Suppress("UNCHECKED_CAST")
                SQLStatement(
                    session,
                    sql,
                    statementType,
                    bindArgs?.copyOfRange(0, parameterCount) as Array<Any?>? ?: arrayOfNulls<Any?>(parameterCount),
                    !(isReadOnly && !statementType.isPredictedWrite)
                )
            }
        }
    }

    override fun bindBlob(index: Int, value: ByteArray) {
        bind(index, value)
    }

    override fun bindDouble(index: Int, value: Double) {
        bind(index, value)
    }

    override fun bindLong(index: Int, value: Long) {
        bind(index, value)
    }

    override fun bindNull(index: Int) {
        bind(index, null)
    }

    override fun bindString(index: Int, value: String) {
        bind(index, value)
    }

    override fun clearBindings() {
        args.fill(null)
    }

    override fun close() = Unit

    override fun execute() {
        session.executeSafely(asWrite, sql, statementType, Unit) { it.execute(sql, args) }
    }

    override fun executeInsert(): Long = session.executeSafely(asWrite, sql, statementType, -1L) {
        it.executeForLastInsertedRowId(sql, args)
    }

    override fun executeUpdateDelete() = session.executeSafely(asWrite, sql, statementType, 0) {
        it.executeForChangedRowCount(sql, args)
    }

    override fun simpleQueryForLong() = session.executeSafely(asWrite, sql, statementType, -1L) {
        it.executeForLong(sql, args)
    }

    override fun simpleQueryForString() = session.executeSafely(asWrite, sql, statementType, null) {
        it.executeForString(sql, args)
    }

    private fun bind(index: Int, value: Any?) {
        args[index - 1] = value
    }
}

@Suppress("ArrayInDataClass")
internal data class SQLStatementInformation(
    val isReadOnly: Boolean,
    val parameterCount: Int,
    val columnNames: Array<out String>
)
