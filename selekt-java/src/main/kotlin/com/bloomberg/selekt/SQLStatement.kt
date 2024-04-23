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

package com.bloomberg.selekt

import com.bloomberg.selekt.commons.isNotEnglishLetter
import com.bloomberg.selekt.commons.trimStartByIndex
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

@JvmSynthetic
@Suppress("Detekt.CognitiveComplexMethod")
internal fun String.resolvedSqlStatementType() = trimStartByIndex(Char::isNotEnglishLetter).run {
    if (length < SUFFICIENT_SQL_PREFIX_LENGTH) {
        return SQLStatementType.OTHER
    }
    when (this[0].uppercaseChar()) {
        'S' -> when (this[1].uppercaseChar()) {
            'E' -> SQLStatementType.SELECT
            else -> SQLStatementType.OTHER // SAVEPOINT, ...
        }
        'I', 'U' -> SQLStatementType.UPDATE
        'D' -> when (this[2].uppercaseChar()) {
            'L' -> SQLStatementType.UPDATE // DELETE
            'O' -> SQLStatementType.DDL
            'T' -> SQLStatementType.UNPREPARED // DETACH
            else -> SQLStatementType.OTHER
        }
        'R' -> when (this[2].uppercaseChar()) {
            'L' -> SQLStatementType.ABORT // TODO what about to savepoint, "ROLLBACK TO ..."? In that case, map to OTHER.
            'P' -> SQLStatementType.UPDATE // REPLACE
            else -> SQLStatementType.OTHER
        }
        'B' -> SQLStatementType.BEGIN
        'C' -> when (this[1].uppercaseChar()) {
            'O' -> SQLStatementType.COMMIT
            'R' -> SQLStatementType.DDL // CREATE
            else -> SQLStatementType.OTHER
        }
        'E' -> when (this[1].uppercaseChar()) {
            'N' -> SQLStatementType.COMMIT
            else -> SQLStatementType.OTHER
        }
        'P' -> SQLStatementType.PRAGMA
        'A' -> when (this[1].uppercaseChar()) {
            'L' -> SQLStatementType.DDL // ALTER
            'T' -> SQLStatementType.ATTACH
            'N' -> SQLStatementType.UNPREPARED
            else -> SQLStatementType.OTHER
        }
        else -> SQLStatementType.OTHER
    }
}

@ThreadSafe
internal class SQLStatement private constructor(
    private val session: ThreadLocalSession,
    private val sql: String,
    private val statementType: SQLStatementType,
    private val args: Array<Any?>,
    private val asWrite: Boolean
) : ISQLStatement {
    companion object {
        fun execute(
            session: ThreadLocalSession,
            sql: String,
            statementType: SQLStatementType,
            bindArgs: Array<out Any?>
        ) = session.get().execute(statementType.isPredictedWrite, sql) {
            it.execute(sql, bindArgs)
        }

        fun execute(
            session: ThreadLocalSession,
            sql: String,
            bindArgs: Sequence<Array<out Any?>>
        ): Int {
            require(SQLStatementType.UPDATE === sql.resolvedSqlStatementType()) {
                "Only batched updates are permitted."
            }
            return session.get().execute(true, sql) {
                it.executeForChangedRowCount(sql, bindArgs)
            }
        }

        fun executeForInt(
            session: ThreadLocalSession,
            sql: String,
            statementType: SQLStatementType,
            bindArgs: Array<out Any?>
        ) = session.get().execute(statementType.isPredictedWrite, sql) {
            it.executeForInt(sql, bindArgs)
        }

        fun executeForString(
            session: ThreadLocalSession,
            sql: String,
            statementType: SQLStatementType,
            bindArgs: Array<out Any?>
        ) = session.get().execute(statementType.isPredictedWrite, sql) {
            it.executeForString(sql, bindArgs)
        }

        fun executeInsert(
            session: ThreadLocalSession,
            sql: String,
            bindArgs: Array<out Any?>
        ) = session.get().execute(true, sql) {
            it.executeForLastInsertedRowId(sql, bindArgs)
        }

        fun executeUpdateDelete(
            session: ThreadLocalSession,
            sql: String,
            bindArgs: Array<out Any?>
        ) = session.get().execute(true, sql) {
            it.executeForChangedRowCount(sql, bindArgs)
        }

        fun compile(
            session: ThreadLocalSession,
            sql: String,
            statementType: SQLStatementType,
            bindArgs: Array<out Any?>?
        ): SQLStatement {
            return session.get().execute(statementType.isPredictedWrite, sql) {
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

    override fun bindInt(index: Int, value: Int) {
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

    override fun close() {
        clearBindings()
    }

    override fun execute() {
        session.get().execute(asWrite, sql, statementType, Unit) { it.execute(sql, args) }
    }

    override fun executeInsert(): Long = session.get().execute(asWrite, sql, statementType, -1L) {
        it.executeForLastInsertedRowId(sql, args)
    }

    override fun executeUpdateDelete() = session.get().execute(asWrite, sql, statementType, 0) {
        it.executeForChangedRowCount(sql, args)
    }

    override fun simpleQueryForLong() = session.get().execute(asWrite, sql, statementType, -1L) {
        it.executeForLong(sql, args)
    }

    override fun simpleQueryForString() = session.get().execute(asWrite, sql, statementType, null) {
        it.executeForString(sql, args)
    }

    private fun bind(index: Int, value: Any?) {
        args[index - 1] = value
    }
}

@Suppress("UseDataClass")
internal class SQLStatementInformation(
    val isReadOnly: Boolean,
    val parameterCount: Int,
    val columnNames: Array<out String>
)
