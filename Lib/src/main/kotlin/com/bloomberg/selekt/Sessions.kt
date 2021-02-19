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

import com.bloomberg.selekt.annotations.Generated
import com.bloomberg.selekt.pools.IPooledObject
import com.bloomberg.selekt.pools.TieredObjectPool
import javax.annotation.concurrent.NotThreadSafe

internal typealias SQLExecutorPool = TieredObjectPool<String, CloseableSQLExecutor>

internal class ThreadLocalSession(
    private val pool: SQLExecutorPool
) {
    private val threadLocal = object : ThreadLocal<SQLSession>() {
        override fun initialValue() = SQLSession(this@ThreadLocalSession, pool)
    }
    private var transactingSession: SQLSession? = null
    private var transactingThread: Thread? = null

    internal fun get(): SQLSession = transactingElseGet()

    internal fun onTransactionBegin() {
        threadLocal.apply {
            transactingSession = threadLocal.get()
            transactingThread = Thread.currentThread()
        }
    }

    internal fun onTransactionEnd() {
        threadLocal.apply {
            transactingSession = null
            transactingThread = null
        }
    }

    private fun transactingElseGet(): SQLSession = if (transactingThread === Thread.currentThread()) {
        transactingSession!!
    } else {
        threadLocal.get()
    }
}

@NotThreadSafe
internal class SQLSession(
    private val parent: ThreadLocalSession,
    pool: SQLExecutorPool
) : Session<String, CloseableSQLExecutor>(pool), ISQLTransactor {
    private var depth = 0
    private var successes = 0
    private lateinit var transactionSql: String
    private var transactionListener: SQLTransactionListener? = null

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

    override fun endTransaction() {
        --depth
        --successes
        if (depth == 0) {
            internalEnd()
        } else {
            check(depth > 0) { "Transaction not begun." }
        }
    }

    override val inTransaction: Boolean
        get() = depth > 0

    override fun setTransactionSuccessful() {
        checkInTransaction()
        check(successes == 0) { "This thread's current transaction is already marked as successful." }
        ++successes
    }

    override fun yieldTransaction() = yieldTransaction(0L)

    override fun yieldTransaction(pauseMillis: Long): Boolean {
        checkInTransaction()
        internalEnd()
        if (pauseMillis > 0L) {
            Thread.sleep(pauseMillis)
        }
        internalBegin(transactionSql, transactionListener)
        return true
    }

    @Generated
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
        if (depth == 0) {
            internalBegin(sql, listener)
        }
        ++depth
    }

    private fun internalBegin(sql: String, listener: SQLTransactionListener?) {
        retain(true, sql).runCatching {
            executeWithRetry(sql)
            listener?.let {
                transactionListener = it
                it.onBegin()
            }
        }.exceptionOrNull()?.let {
            rollbackQuietly()
            release()
            throw it
        }
        transactionSql = sql
        parent.onTransactionBegin()
    }

    private fun internalEnd() {
        try {
            if (successes == 0) {
                commit()
            } else {
                successes = 0
                rollback()
            }
        } finally {
            parent.onTransactionEnd()
            transactionListener = null
            release()
        }
    }

    private fun commit() {
        execute(true) {
            runCatching {
                transactionListener?.onCommit()
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
                transactionListener?.onRollback()
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
internal open class Session<K : Any, T : IPooledObject<K>> constructor(
    private val pool: TieredObjectPool<K, T>
) {
    private var obj: T? = null
    private var retainCount = 0

    @Generated
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

    @Generated
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

    protected fun retain(primary: Boolean, key: K) = retain(primary) { pool.borrowObject(key) }

    protected fun release() = obj!!.release()

    private fun retain(primary: Boolean) = retain(primary) { pool.borrowObject() }

    @Generated
    private inline fun retain(primary: Boolean, block: () -> T) =
        (obj ?: (if (primary) pool.borrowPrimaryObject() else block()).also { obj = it }).also { ++retainCount }

    private fun T.release() {
        if (--retainCount == 0) {
            pool.returnObject(this).also { obj = null }
        }
    }
}
