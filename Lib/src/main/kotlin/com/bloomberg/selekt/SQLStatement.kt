/*
 * Copyright 2020 Bloomberg Finance L.P.
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

import java.util.Locale
import javax.annotation.concurrent.ThreadSafe

private fun String.isPredictedWrite() = sqlStatementType().isPredictedWrite

internal enum class SQLStatementType(
    val isPredictedWrite: Boolean = true,
    val isTransactional: Boolean = false,
    val aborts: Boolean = false,
    val commits: Boolean = false,
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

internal fun CharSequence.sqlStatementType() = trimStart(Char::isWhitespace).run {
    if (length < SUFFICIENT_SQL_PREFIX_LENGTH) {
        return SQLStatementType.OTHER
    }
    when (substring(0, SUFFICIENT_SQL_PREFIX_LENGTH).toUpperCase(Locale.ROOT)) {
        "SEL" -> SQLStatementType.SELECT
        "INS", "UPD", "DEL", "REP" -> SQLStatementType.UPDATE
        "BEG" -> SQLStatementType.BEGIN
        "COM", "END" -> SQLStatementType.COMMIT
        "ROL" -> SQLStatementType.ABORT // TODO what about to savepoint, "ROLLBACK TO ..."? In that case, map to OTHER.
        "PRA" -> SQLStatementType.PRAGMA
        "ALT", "CRE", "DRO" -> SQLStatementType.DDL
        "ATT" -> SQLStatementType.ATTACH
        "ANA", "DET" -> SQLStatementType.UNPREPARED
        else -> SQLStatementType.OTHER
    }
}

@ThreadSafe
internal class SQLStatement private constructor(
    private val session: ThreadLocalisedSession,
    private val isRaw: Boolean,
    private val sql: String,
    private val args: Array<Any?>,
    private val asWrite: Boolean
) : ISQLStatement {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun compileTrustedWrite(
            session: ThreadLocalisedSession,
            sql: String,
            bindArgs: Array<out Any?>
        ) = SQLStatement(
            session,
            false,
            sql,
            bindArgs as Array<Any?>,
            true
        )

        fun compile(
            session: ThreadLocalisedSession,
            isRaw: Boolean,
            sql: String,
            bindArgs: Array<out Any?>?
        ): SQLStatement {
            val predictedWrite = sql.isPredictedWrite()
            return session.execute(predictedWrite, sql, false) {
                it.prepare(sql)
            }.run {
                @Suppress("UNCHECKED_CAST")
                SQLStatement(
                    session,
                    isRaw,
                    sql,
                    bindArgs?.copyOfRange(0, parameterCount) as Array<Any?>? ?: arrayOfNulls<Any?>(parameterCount),
                    !(isReadOnly && !predictedWrite)
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

    private fun bind(index: Int, value: Any?) {
        args[index - 1] = value
    }

    override fun clearBindings() {
        args.fill(null)
    }

    override fun close() = Unit

    override fun execute() {
        session.execute(asWrite, sql, isRaw) { it.execute(sql, args) }
    }

    fun executeForInt() = session.execute(asWrite, sql, isRaw) { it.executeForInt(sql, args) }

    override fun executeInsert() = session.execute(asWrite, sql, isRaw) {
        it.executeForLastInsertedRowId(sql, args)
    }

    override fun executeUpdateDelete() = session.execute(asWrite, sql, isRaw) {
        it.executeForChangedRowCount(sql, args)
    }

    override fun simpleQueryForLong() = session.execute(asWrite, sql, isRaw) { it.executeForLong(sql, args) }

    fun executeForString() = session.execute(asWrite, sql, isRaw) { it.executeForString(sql, args) }

    override fun simpleQueryForString() = executeForString()
}

@Suppress("ArrayInDataClass")
internal data class SQLStatementInformation(
    val columnNames: Array<out String>,
    val isReadOnly: Boolean,
    val parameterCount: Int
)
