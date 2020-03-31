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

import com.bloomberg.commons.threadLocal
import com.bloomberg.selekt.pools.IObjectPool
import javax.annotation.concurrent.NotThreadSafe

internal typealias SQLExecutorPool = IObjectPool<String, CloseableSQLExecutor>

internal class ThreadLocalisedSession(pool: SQLExecutorPool) {
    private val session by threadLocal { SQLSession(pool) }

    val hasObject: Boolean
        get() = session.hasObject

    fun beginDeferredTransaction() = session.beginDeferredTransaction()

    fun beginExclusiveTransaction() = session.beginExclusiveTransaction()

    fun beginImmediateTransaction() = session.beginImmediateTransaction()

    fun endTransaction() = session.endTransaction()

    val inTransaction: Boolean
        get() = session.inTransaction

    fun setTransactionSuccessful() = session.setTransactionSuccessful()

    fun yieldTransaction() = session.yieldTransaction()

    fun yieldTransaction(pauseMillis: Long) = session.yieldTransaction(pauseMillis)

    internal inline fun <R> execute(
        primary: Boolean,
        sql: String,
        mustValidate: Boolean,
        block: (SQLExecutor) -> R
    ) = session.execute(primary, sql, mustValidate, block)
}

@NotThreadSafe
internal class SQLSession(
    pool: SQLExecutorPool
) : Session<String, CloseableSQLExecutor>(pool), ISQLTransactor {
    private val proxy = SQLExecutorProxy(this)
    private var depth = 0
    private var successes = 0
    private lateinit var transactionSql: String

    override fun beginDeferredTransaction() = begin(TransactionMode.DEFERRED)

    override fun beginExclusiveTransaction() = begin(TransactionMode.EXCLUSIVE)

    override fun beginImmediateTransaction() = begin(TransactionMode.IMMEDIATE)

    fun beginRawTransaction(sql: String) = begin(sql)

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
        internalBegin(transactionSql)
        return true
    }

    internal inline fun <R> execute(
        primary: Boolean,
        sql: String,
        mustValidate: Boolean,
        block: (SQLExecutor) -> R
    ) = execute(primary, sql) {
        if (mustValidate) {
            try {
                block(proxy.apply { executor = it })
            } finally {
                proxy.reset()
            }
        } else {
            block(it)
        }
    }

    private fun begin(mode: TransactionMode) = begin(mode.sql)

    private fun begin(sql: String) {
        if (depth == 0) {
            internalBegin(sql)
        }
        ++depth
    }

    private fun internalBegin(sql: String) {
        transactionSql = sql
        retain(true, sql).runCatching {
            executeWithRetry(sql)
        }.exceptionOrNull()?.let {
            rollbackQuietly()
            release()
            throw it
        }
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
            release()
        }
    }

    private fun commit() = execute(true, "END") {
        runCatching { it.executeWithRetry("END") }.exceptionOrNull()?.let {
            rollbackQuietly()
            throw it
        }
    }

    private fun rollback() = execute(false, "ROLLBACK") { it.execute("ROLLBACK") }

    private fun rollbackQuietly() {
        runCatching { rollback() }
    }

    private fun checkInTransaction() = check(inTransaction) { "This thread is not in a transaction." }
}

internal class SQLExecutorProxy(
    private val session: SQLSession
) : CloseableSQLExecutor {
    internal var executor: CloseableSQLExecutor = StubCloseableSQLExecutor

    private inline fun <R> internalExecute(
        sql: String,
        defaultValue: R,
        block: () -> R
    ) = session.run {
        sql.sqlStatementType().let {
            if (!it.isTransactional) {
                return block()
            }
            when {
                it.begins -> beginRawTransaction(sql)
                it.commits -> {
                    setTransactionSuccessful()
                    endTransaction()
                }
                it.aborts -> endTransaction()
                else -> error("Unrecognised transactional statement type: $it")
            }
            defaultValue
        }
    }

    internal fun reset() {
        executor = StubCloseableSQLExecutor
    }

    override val isAutoCommit: Boolean
        get() = executor.isAutoCommit

    override val isPrimary: Boolean
        get() = executor.isPrimary

    override val isReadOnly: Boolean
        get() = executor.isReadOnly

    override fun checkpoint(name: String?, mode: SQLCheckpointMode) = executor.checkpoint(name, mode)

    override fun close() = executor.close()

    override fun execute(sql: String, bindArgs: Array<*>) = internalExecute(sql, 0) {
        executor.execute(sql, bindArgs)
    }

    override fun executeForChangedRowCount(sql: String, bindArgs: Array<*>) = internalExecute(sql, 0) {
        executor.executeForChangedRowCount(sql, bindArgs)
    }

    override fun executeForCursorWindow(sql: String, bindArgs: Array<*>, window: ICursorWindow) =
        internalExecute(sql, Unit) { executor.executeForCursorWindow(sql, bindArgs, window) }

    override fun executeForInt(sql: String, bindArgs: Array<*>) = internalExecute(sql, 0) {
        executor.executeForInt(sql, bindArgs)
    }

    override fun executeForLastInsertedRowId(sql: String, bindArgs: Array<*>) = internalExecute(sql, 0L) {
        executor.executeForLastInsertedRowId(sql, bindArgs)
    }

    override fun executeForLong(sql: String, bindArgs: Array<*>) = internalExecute(sql, 0L) {
        executor.executeForLong(sql, bindArgs)
    }

    override fun executeForString(sql: String, bindArgs: Array<*>) = internalExecute(sql, null) {
        executor.executeForString(sql, bindArgs)
    }

    override fun executeWithRetry(sql: String) = internalExecute(sql, 0) {
        executor.executeWithRetry(sql)
    }

    override fun matches(key: String) = executor.matches(key)

    override fun prepare(sql: String) = executor.prepare(sql)
}

private object StubCloseableSQLExecutor : CloseableSQLExecutor {
    override val isAutoCommit: Boolean
        get() = throw UnsupportedOperationException()

    override val isPrimary: Boolean
        get() = throw UnsupportedOperationException()

    override val isReadOnly: Boolean
        get() = throw UnsupportedOperationException()

    override fun checkpoint(name: String?, mode: SQLCheckpointMode) = throw UnsupportedOperationException()

    override fun close() = throw UnsupportedOperationException()

    override fun execute(sql: String, bindArgs: Array<*>) = throw UnsupportedOperationException()

    override fun executeForChangedRowCount(sql: String, bindArgs: Array<*>) = throw UnsupportedOperationException()

    override fun executeForCursorWindow(sql: String, bindArgs: Array<*>, window: ICursorWindow) =
        throw UnsupportedOperationException()

    override fun executeForInt(sql: String, bindArgs: Array<*>) = throw UnsupportedOperationException()

    override fun executeForLastInsertedRowId(sql: String, bindArgs: Array<*>) = throw UnsupportedOperationException()

    override fun executeForLong(sql: String, bindArgs: Array<*>) = throw UnsupportedOperationException()

    override fun executeForString(sql: String, bindArgs: Array<*>) = throw UnsupportedOperationException()

    override fun executeWithRetry(sql: String) = throw UnsupportedOperationException()

    override fun matches(key: String) = throw UnsupportedOperationException()

    override fun prepare(sql: String) = throw UnsupportedOperationException()
}

interface ISQLTransactor {
    val inTransaction: Boolean

    fun beginDeferredTransaction()

    fun beginExclusiveTransaction()

    fun beginImmediateTransaction()

    fun endTransaction()

    fun setTransactionSuccessful()

    fun yieldTransaction(): Boolean

    fun yieldTransaction(pauseMillis: Long): Boolean
}

@NotThreadSafe
internal open class Session<K : Any, T : Any> constructor(
    private val pool: IObjectPool<K, T>
) {
    private var obj: T? = null
    private var retainCount = 0

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

    val hasObject: Boolean
        get() = retainCount > 0

    protected fun retain(primary: Boolean, key: K) =
        (obj ?: (if (primary) pool.borrowPrimaryObject() else pool.borrowObject(key)).also { obj = it })
            .also { ++retainCount }

    protected fun release() = checkNotNull(obj).release()

    private fun T.release() {
        check(retainCount > 0) { "Attempting to over-release a resource." }
        if (--retainCount == 0) {
            pool.returnObject(this).also { obj = null }
        }
    }
}

private enum class TransactionMode {
    DEFERRED,
    EXCLUSIVE,
    IMMEDIATE;

    val sql = "BEGIN $name"
}
