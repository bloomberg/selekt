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
import java.util.stream.Stream
import javax.annotation.concurrent.ThreadSafe
import kotlin.streams.asSequence

/**
 * @since 0.12.1
 */
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
    SAVEPOINT(isTransactional = true),
    SELECT(false),
    UNPREPARED,
    UPDATE
}

private const val SUFFICIENT_SQL_PREFIX_LENGTH = 3
private const val ONLY_BATCH_UPDATES = "Only batched updates are permitted."

private class SQLTokenCursor(private val sql: String) {
    private var index = 0
    private var tokenStart = 0
    private var tokenLength = 0

    fun firstTokenIndex(): Int {
        skipSeparators()
        return index
    }

    fun next(): Boolean {
        skipSeparators()
        tokenLength = 0
        if (index >= sql.length || sql[index].isNotEnglishLetter()) {
            return false
        }
        tokenStart = index
        while (index < sql.length && !sql[index].isNotEnglishLetter()) {
            ++index
        }
        tokenLength = index - tokenStart
        return true
    }

    fun tokenEquals(expected: String): Boolean =
        tokenLength == expected.length && sql.regionMatches(tokenStart, expected, 0, tokenLength, ignoreCase = true)

    private fun skipSeparators() {
        while (index < sql.length) {
            when {
                sql.startsWith("--", index) -> skipLineComment()
                sql.startsWith("/*", index) -> skipBlockComment()
                sql[index].isNotEnglishLetter() -> ++index
                else -> return
            }
        }
    }

    private fun skipLineComment() {
        index = sql.indexOfAny(charArrayOf('\n', '\r'), index + 2).let { end ->
            if (end < 0) { sql.length } else { end + 1 }
        }
    }

    private fun skipBlockComment() {
        index = sql.indexOf("*/", index + 2).let { end ->
            if (end < 0) { sql.length } else { end + 2 }
        }
    }
}

private fun SQLTokenCursor.resolveTransactionStatementType(): SQLStatementType? = if (!next()) {
    null
} else {
    when {
        tokenEquals("BEGIN") -> SQLStatementType.BEGIN
        tokenEquals("COMMIT") || tokenEquals("END") -> SQLStatementType.COMMIT
        tokenEquals("ROLLBACK") -> if (isToClauseAfterOptionalTransaction()) {
            SQLStatementType.SAVEPOINT
        } else {
            SQLStatementType.ABORT
        }
        tokenEquals("SAVEPOINT") || tokenEquals("RELEASE") -> SQLStatementType.SAVEPOINT
        else -> null
    }
}

private fun SQLTokenCursor.isToClauseAfterOptionalTransaction(): Boolean =
    next() && if (tokenEquals("TRANSACTION")) { next() && tokenEquals("TO") } else { tokenEquals("TO") }

@JvmSynthetic
internal fun String.isRollbackToSavepointClause(): Boolean {
    val cursor = SQLTokenCursor(this)
    return cursor.next() && cursor.tokenEquals("ROLLBACK") && cursor.isToClauseAfterOptionalTransaction()
}

@JvmSynthetic
internal fun String.resolvedSqlStatementType(): SQLStatementType {
    val tokenCursor = SQLTokenCursor(this)
    val firstTokenIndex = tokenCursor.firstTokenIndex()
    tokenCursor.resolveTransactionStatementType()?.let { return it }
    val sql = substring(firstTokenIndex)
    return if (sql.length < SUFFICIENT_SQL_PREFIX_LENGTH) {
        SQLStatementType.OTHER
    } else {
        sql.statementTypeFromPrefix()
    }
}

private fun String.statementTypeFromPrefix(): SQLStatementType = when (this[0].uppercaseChar()) {
    'S' -> statementTypeStartingWithS()
    'I', 'U' -> SQLStatementType.UPDATE
    'D' -> statementTypeStartingWithD()
    'R' -> statementTypeStartingWithR()
    'B' -> SQLStatementType.BEGIN
    'C' -> statementTypeStartingWithC()
    'E' -> statementTypeStartingWithE()
    'P' -> SQLStatementType.PRAGMA
    'A' -> statementTypeStartingWithA()
    else -> SQLStatementType.OTHER
}

private fun String.statementTypeStartingWithS(): SQLStatementType = when (this[1].uppercaseChar()) {
    'E' -> SQLStatementType.SELECT
    else -> SQLStatementType.OTHER // SAVEPOINT, ...
}

private fun String.statementTypeStartingWithD(): SQLStatementType = when (this[2].uppercaseChar()) {
    'L' -> SQLStatementType.UPDATE // DELETE
    'O' -> SQLStatementType.DDL
    'T' -> SQLStatementType.UNPREPARED // DETACH
    else -> SQLStatementType.OTHER
}

private fun String.statementTypeStartingWithR(): SQLStatementType = when (this[2].uppercaseChar()) {
    'L' -> rollbackStatementType()
    'P' -> SQLStatementType.UPDATE // REPLACE
    else -> SQLStatementType.OTHER
}

private fun String.rollbackStatementType(): SQLStatementType =
    if ('E' == this[1].uppercaseChar() || isRollbackToSavepointClause()) {
        SQLStatementType.OTHER // RELEASE, or ROLLBACK [TRANSACTION] TO savepoint
    } else {
        SQLStatementType.ABORT // ROLLBACK, ROLLBACK TRANSACTION
    }

private fun String.statementTypeStartingWithC(): SQLStatementType = when (this[1].uppercaseChar()) {
    'O' -> SQLStatementType.COMMIT
    'R' -> SQLStatementType.DDL // CREATE
    else -> SQLStatementType.OTHER
}

private fun String.statementTypeStartingWithE(): SQLStatementType = when (this[1].uppercaseChar()) {
    'N' -> SQLStatementType.COMMIT
    else -> SQLStatementType.OTHER
}

private fun String.statementTypeStartingWithA(): SQLStatementType = when (this[1].uppercaseChar()) {
    'L' -> SQLStatementType.DDL // ALTER
    'T' -> SQLStatementType.ATTACH
    'N' -> SQLStatementType.UNPREPARED
    else -> SQLStatementType.OTHER
}

@Suppress("Detekt.MethodOverloading")
@ThreadSafe
internal class SQLStatement private constructor(
    private val session: SQLSessionProvider,
    private val sql: String,
    private val statementType: SQLStatementType,
    private val args: Array<Any?>,
    private val asWrite: Boolean,
    override val isReadOnly: Boolean
) : ISQLStatement {
    private val namedParameters: Map<String, Int> by lazy { parseNamedParameters(sql) }

    @Suppress("Detekt.TooManyFunctions")
    companion object {
        fun execute(
            session: SQLSessionProvider,
            sql: String,
            statementType: SQLStatementType,
            bindArgs: Array<out Any?>
        ) = session().execute(statementType.isPredictedWrite, sql) {
            it.execute(sql, bindArgs)
        }

        fun execute(
            session: SQLSessionProvider,
            sql: String,
            bindArgs: Sequence<Array<out Any?>>
        ): Int {
            require(SQLStatementType.UPDATE === sql.resolvedSqlStatementType()) { ONLY_BATCH_UPDATES }
            return session().execute(true, sql) {
                it.executeBatchForChangedRowCount(sql, bindArgs)
            }
        }

        fun execute(
            session: SQLSessionProvider,
            sql: String,
            bindArgs: List<Array<out Any?>>
        ): Int {
            require(SQLStatementType.UPDATE === sql.resolvedSqlStatementType()) { ONLY_BATCH_UPDATES }
            return session().execute(true, sql) {
                it.executeBatchForChangedRowCount(sql, bindArgs)
            }
        }

        fun execute(
            session: SQLSessionProvider,
            sql: String,
            bindArgs: Array<out Array<out Any?>>,
            fromIndex: Int = 0,
            toIndex: Int = bindArgs.size
        ): Int {
            require(SQLStatementType.UPDATE === sql.resolvedSqlStatementType()) { ONLY_BATCH_UPDATES }
            return session().execute(true, sql) {
                it.executeBatchForChangedRowCount(sql, bindArgs, fromIndex, toIndex)
            }
        }

        fun execute(
            session: SQLSessionProvider,
            sql: String,
            bindArgs: Iterable<Array<out Any?>>
        ): Int {
            require(SQLStatementType.UPDATE === sql.resolvedSqlStatementType()) { ONLY_BATCH_UPDATES }
            return session().execute(true, sql) {
                it.executeBatchForChangedRowCount(sql, bindArgs)
            }
        }

        fun executeRows(
            session: SQLSessionProvider,
            sql: String,
            bindArgs: Iterable<ParameterRow>
        ): Int {
            require(SQLStatementType.UPDATE === sql.resolvedSqlStatementType()) { ONLY_BATCH_UPDATES }
            return session().execute(true, sql) {
                it.executeBatchForChangedRowCountRows(sql, bindArgs)
            }
        }

        fun execute(
            session: SQLSessionProvider,
            sql: String,
            bindArgs: Stream<Array<out Any?>>
        ) = execute(session, sql, bindArgs.asSequence())

        fun executeForInt(
            session: SQLSessionProvider,
            sql: String,
            statementType: SQLStatementType,
            bindArgs: Array<out Any?>
        ) = session().execute(statementType.isPredictedWrite, sql) {
            it.executeForInt(sql, bindArgs)
        }

        fun executeForString(
            session: SQLSessionProvider,
            sql: String,
            statementType: SQLStatementType,
            bindArgs: Array<out Any?>
        ) = session().execute(statementType.isPredictedWrite, sql) {
            it.executeForString(sql, bindArgs)
        }

        fun executeInsert(
            session: SQLSessionProvider,
            sql: String,
            bindArgs: Array<out Any?>
        ) = session().execute(true, sql) {
            it.executeForLastInsertedRowId(sql, bindArgs)
        }

        fun executeUpdateDelete(
            session: SQLSessionProvider,
            sql: String,
            bindArgs: Array<out Any?>
        ) = session().execute(true, sql) {
            it.executeForChangedRowCount(sql, bindArgs)
        }

        fun compile(
            session: SQLSessionProvider,
            sql: String,
            statementType: SQLStatementType,
            bindArgs: Array<out Any?>?
        ): SQLStatement {
            return session().execute(statementType.isPredictedWrite, sql) {
                it.prepare(sql)
            }.run {
                @Suppress("UNCHECKED_CAST")
                SQLStatement(
                    session,
                    sql,
                    statementType,
                    bindArgs?.copyOfRange(0, parameterCount) as Array<Any?>? ?: arrayOfNulls<Any?>(parameterCount),
                    !(isReadOnly && !statementType.isPredictedWrite),
                    isReadOnly
                )
            }
        }
    }

    override fun bindBlob(index: Int, value: ByteArray) {
        bind(index, value)
    }

    override fun bindBlob(name: String, value: ByteArray) {
        bind(resolveParameterIndex(name), value)
    }

    override fun bindDouble(index: Int, value: Double) {
        bind(index, value)
    }

    override fun bindDouble(name: String, value: Double) {
        bind(resolveParameterIndex(name), value)
    }

    override fun bindInt(index: Int, value: Int) {
        bind(index, value)
    }

    override fun bindInt(name: String, value: Int) {
        bind(resolveParameterIndex(name), value)
    }

    override fun bindLong(index: Int, value: Long) {
        bind(index, value)
    }

    override fun bindLong(name: String, value: Long) {
        bind(resolveParameterIndex(name), value)
    }

    override fun bindNull(index: Int) {
        bind(index, null)
    }

    override fun bindNull(name: String) {
        bind(resolveParameterIndex(name), null)
    }

    override fun bindString(index: Int, value: String) {
        bind(index, value)
    }

    override fun bindString(name: String, value: String) {
        bind(resolveParameterIndex(name), value)
    }

    override fun clearBindings() {
        args.fill(null)
    }

    override fun close() {
        clearBindings()
    }

    override fun execute() {
        session().execute(asWrite, sql, statementType, Unit) { it.execute(sql, args) }
    }

    override fun executeInsert(): Long = session().execute(asWrite, sql, statementType, -1L) {
        it.executeForLastInsertedRowId(sql, args)
    }

    override fun executeUpdateDelete() = session().execute(asWrite, sql, statementType, 0) {
        it.executeForChangedRowCount(sql, args)
    }

    override fun simpleQueryForLong() = session().execute(asWrite, sql, statementType, -1L) {
        it.executeForLong(sql, args)
    }

    override fun simpleQueryForString() = session().execute(asWrite, sql, statementType, null) {
        it.executeForString(sql, args)
    }

    private fun bind(index: Int, value: Any?) {
        args[index - 1] = value
    }

    private fun resolveParameterIndex(name: String): Int =
        namedParameters[name] ?: throw IllegalArgumentException(
            "Named parameter '$name' not found in SQL. Available parameters: ${namedParameters.keys}"
        )
}

@Suppress("UseDataClass")
internal class SQLStatementInformation(
    val isReadOnly: Boolean,
    val parameterCount: Int,
    val columnNames: Array<out String>,
    val columnMetadata: Array<out ColumnMetadata>
)
