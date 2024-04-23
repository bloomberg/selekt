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

import com.bloomberg.selekt.pools.IPooledObject
import com.bloomberg.selekt.pools.TieredObjectPool
import javax.annotation.concurrent.NotThreadSafe

internal typealias SQLExecutorPool = TieredObjectPool<String, CloseableSQLExecutor>

internal class ThreadLocalSession(
    private val pool: SQLExecutorPool
) {
    private val threadLocal = object : ThreadLocal<SQLSession>() {
        override fun initialValue() = SQLSession(pool)
    }

    @JvmSynthetic
    internal fun get(): SQLSession = threadLocal.get()
}

private data class SQLSessionState(
    var depth: Int = 0,
    var successes: Int = 0,
    var transactionSql: String = "",
    var transactionListener: SQLTransactionListener? = null
) {
    fun clear() {
        depth = 0
        successes = 0
        transactionSql = ""
        transactionListener = null
    }
}

@NotThreadSafe
internal class SQLSession(
    pool: SQLExecutorPool
) : Session<String, CloseableSQLExecutor>(pool), ISQLTransactor {
    private var state = SQLSessionState()

    override fun beginExclusiveTransaction() = begin(SQLiteTransactionMode.EXCLUSIVE, null)

    override fun beginExclusiveTransactionWithListener(listener: SQLTransactionListener) =
        begin(SQLiteTransactionMode.EXCLUSIVE, listener)

    override fun beginImmediateTransaction() = begin(SQLiteTransactionMode.IMMEDIATE, null)

    override fun beginImmediateTransactionWithListener(listener: SQLTransactionListener) =
        begin(SQLiteTransactionMode.IMMEDIATE, listener)

    fun beginRawTransaction(sql: String) = begin(sql, null)

    fun blob(
        name: String,
        table: String,
        column: String,
        row: Long,
        readOnly: Boolean
    ) = execute(!readOnly) {
        it.executeForBlob(name, table, column, row)
    }

    override fun endTransaction() = state.run {
        --depth
        --successes
        if (depth == 0) {
            internalEnd()
        } else {
            check(depth > 0) { "Transaction not begun." }
        }
    }

    override val inTransaction: Boolean
        get() = state.depth > 0

    override fun setTransactionSuccessful() {
        checkInTransaction()
        check(state.successes == 0) { "This thread's current transaction is already marked as successful." }
        ++state.successes
    }

    override fun yieldTransaction() = yieldTransaction(0L)

    override fun yieldTransaction(pauseMillis: Long): Boolean {
        checkInTransaction()
        check(state.successes == 0) { "This thread's current transaction must not have been marked as successful yet." }
        val oldState = state.copy()
        val oldRetainCount = retainCount
        internalEnd(oldRetainCount)
        if (pauseMillis > 0L) {
            Thread.sleep(pauseMillis)
        }
        internalBegin(oldState.transactionSql, oldState.transactionListener, oldRetainCount)
        state = oldState
        return true
    }

    internal inline fun <R> execute(
        primary: Boolean,
        sql: String,
        statementType: SQLStatementType,
        signal: R,
        block: (SQLExecutor) -> R
    ): R = execute(primary, sql) {
        if (!statementType.isTransactional) {
            return block(it)
        }
        when {
            statementType.begins -> beginRawTransaction(sql)
            statementType.commits -> {
                setTransactionSuccessful()
                endTransaction()
            }
            statementType.aborts -> endTransaction()
            else -> error("Unrecognised statement type: $statementType")
        }
        signal
    }

    private fun begin(
        mode: SQLiteTransactionMode,
        listener: SQLTransactionListener?
    ) = begin(mode.sql, listener)

    private fun begin(sql: String, listener: SQLTransactionListener?) {
        if (state.depth == 0) {
            internalBegin(sql, listener)
        }
        ++state.depth
    }

    private fun internalBegin(
        sql: String,
        listener: SQLTransactionListener?,
        permits: Int = 1
    ) {
        retain(true, sql, permits).runCatching {
            executeWithRetry(sql)
            listener?.let {
                state.transactionListener = it
                it.onBegin()
            }
        }.exceptionOrNull()?.let {
            rollbackQuietly()
            state.clear()
            release(permits)
            throw it
        }
        state.transactionSql = sql
    }

    private fun internalEnd(permits: Int = 1) {
        try {
            if (state.successes == 0) {
                commit()
            } else {
                rollback()
            }
        } finally {
            state.clear()
            release(permits)
        }
    }

    private fun commit() {
        execute(true) {
            runCatching {
                state.transactionListener?.onCommit()
                it.executeWithRetry("END")
            }.exceptionOrNull()?.let {
                rollbackQuietly()
                throw it
            }
        }
    }

    private fun rollback() {
        execute(false) {
            try {
                state.transactionListener?.onRollback()
            } finally {
                it.execute("ROLLBACK")
            }
        }
    }

    private fun rollbackQuietly() {
        runCatching { rollback() }
    }

    private fun checkInTransaction() = check(inTransaction) { "This thread is not in a transaction." }
}

interface ISQLTransactor {
    val inTransaction: Boolean

    fun beginExclusiveTransaction()

    fun beginExclusiveTransactionWithListener(listener: SQLTransactionListener)

    fun beginImmediateTransaction()

    fun beginImmediateTransactionWithListener(listener: SQLTransactionListener)

    fun endTransaction()

    fun setTransactionSuccessful()

    fun yieldTransaction(): Boolean

    fun yieldTransaction(pauseMillis: Long): Boolean
}

@NotThreadSafe
internal open class Session<K : Any, T : IPooledObject<K>>(
    private val pool: TieredObjectPool<K, T>
) {
    private var obj: T? = null
    protected var retainCount = 0

    inline fun <R> execute(
        primary: Boolean,
        key: K,
        block: (T) -> R
    ) = retain(primary, key).run {
        try {
            block(this)
        } finally {
            release()
        }
    }

    inline fun <R> execute(
        primary: Boolean,
        block: (T) -> R
    ) = retain(primary).run {
        try {
            block(this)
        } finally {
            release()
        }
    }

    val hasObject: Boolean
        get() = retainCount > 0

    protected fun retain(
        primary: Boolean,
        key: K,
        permits: Int = 1
    ) = retain(primary, permits) { pool.borrowObject(key) }

    protected fun release(permits: Int = 1) = obj!!.release(permits)

    private fun retain(primary: Boolean, permits: Int = 1) = retain(primary, permits) { pool.borrowObject() }

    private inline fun retain(
        primary: Boolean,
        permits: Int,
        block: () -> T
    ) = (obj ?: (if (primary) pool.borrowPrimaryObject() else block()).also { obj = it }).also {
        retainCount += permits
    }

    private fun T.release(permits: Int) {
        retainCount -= permits
        if (retainCount == 0) {
            pool.returnObject(this).also { obj = null }
        }
    }
}
