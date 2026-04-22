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
    private val pool: SQLExecutorPool,
    private val useNativeListeners: Boolean = false
) {
    private val threadLocal = object : ThreadLocal<SQLSession>() {
        override fun initialValue() = SQLSession(pool, useNativeListeners)
    }

    @JvmSynthetic
    internal operator fun invoke(): SQLSession = threadLocal.get()
}

private data class SavepointInfo(
    val name: String,
    var successful: Boolean = false,
    val automatic: Boolean = true
)

private class SQLSessionState(
    var inOuterTransaction: Boolean = false,
    var outerSuccessful: Boolean = false,
    var savepointStack: ArrayList<SavepointInfo> = ArrayList(),
    var transactionSql: String = "",
    var transactionListener: SQLTransactionListener? = null
) {
    fun clear() {
        inOuterTransaction = false
        outerSuccessful = false
        savepointStack = ArrayList()
        transactionSql = ""
        transactionListener = null
    }
}

private fun <T> MutableList<T>.removeLast() = removeAt(lastIndex)

private fun requireSafeSavepointName(name: String) {
    require(name.isNotEmpty() && (name.first().isLetter() || name.first() == '_')) {
        "Invalid savepoint name: '$name'. Must start with a letter or underscore."
    }
    for (c in name) {
        require(c.isLetterOrDigit() || c == '_') {
            "Invalid savepoint name: '$name'. Must contain only letters, digits, or underscores."
        }
    }
}

@NotThreadSafe
internal class SQLSession(
    pool: SQLExecutorPool,
    private val useNativeListeners: Boolean = false
) : Session<String, CloseableSQLExecutor>(pool), ISQLTransactor {
    private val state = SQLSessionState()

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

    override fun endTransaction(): Unit = state.run {
        val autoSavepointIndex = savepointStack.indexOfLast { it.automatic }
        if (autoSavepointIndex < 0) {
            check(inOuterTransaction) { "Transaction not begun." }
            internalEnd()
            return
        }
        savepointStack[autoSavepointIndex].let {
            if (!it.successful) {
                execute(true) { executor ->
                    executor.runCatching {
                        execute("ROLLBACK TO ${it.name}")
                        execute("RELEASE ${it.name}")
                    }
                }
            }
            while (savepointStack.size > autoSavepointIndex) {
                savepointStack.removeLast()
            }
        }
    }

    override val inTransaction: Boolean
        get() = state.inOuterTransaction

    override fun setTransactionSuccessful() {
        checkInTransaction()
        val autoSavepointIndex = state.savepointStack.indexOfLast(SavepointInfo::automatic)
        if (autoSavepointIndex >= 0) {
            val info = state.savepointStack[autoSavepointIndex]
            check(!info.successful) { "This savepoint is already marked as successful." }
            info.successful = true
        } else {
            check(!state.outerSuccessful) { "This thread's current transaction is already marked as successful." }
            state.outerSuccessful = true
        }
    }

    override fun yieldTransaction() = yieldTransaction(0L)

    override fun yieldTransaction(pauseMillis: Long): Boolean {
        checkInTransaction()
        check(!state.outerSuccessful) { "This thread's current transaction must not have been marked as successful yet." }
        val oldSavepointStack = state.savepointStack
        val oldTransactionSql = state.transactionSql
        val oldTransactionListener = state.transactionListener
        val oldRetainCount = retainCount
        state.outerSuccessful = true
        internalEnd(oldRetainCount)
        if (pauseMillis > 0L) {
            Thread.sleep(pauseMillis)
        }
        internalBegin(oldTransactionSql, oldTransactionListener, oldRetainCount)
        state.apply {
            inOuterTransaction = true
            outerSuccessful = false
            savepointStack = oldSavepointStack
            transactionSql = oldTransactionSql
            transactionListener = oldTransactionListener
        }
        if (oldSavepointStack.isNotEmpty()) {
            execute(true) {
                oldSavepointStack.forEach { savepointInfo ->
                    it.execute("SAVEPOINT ${savepointInfo.name}")
                }
            }
        }
        return true
    }

    fun setSavepoint(name: String? = null): String {
        checkInTransaction()
        val savepointName = name?.also(::requireSafeSavepointName) ?: "sp_user_${state.savepointStack.size}"
        execute(true) {
            it.execute("SAVEPOINT $savepointName")
        }
        state.savepointStack.add(SavepointInfo(savepointName, successful = false, automatic = false))
        return savepointName
    }

    fun rollbackToSavepoint(name: String) {
        requireSafeSavepointName(name)
        checkInTransaction()
        val index = state.savepointStack.indexOfLast { it.name == name }
        check(index >= 0) { "Savepoint $name not found" }
        execute(true) {
            it.execute("ROLLBACK TO $name")
        }
        while (state.savepointStack.size > index + 1) {
            state.savepointStack.removeLast()
        }
    }

    fun releaseSavepoint(name: String) {
        requireSafeSavepointName(name)
        checkInTransaction()
        val index = state.savepointStack.indexOfLast { it.name == name }
        if (index < 0) {
            return
        }
        execute(true) {
            it.execute("RELEASE $name")
        }
        while (state.savepointStack.size > index) {
            state.savepointStack.removeLast()
        }
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
        if (!state.inOuterTransaction) {
            internalBegin(sql, listener)
            state.inOuterTransaction = true
        } else {
            SavepointInfo("sp_auto_${state.savepointStack.size}").let {
                execute(true) { executor ->
                    executor.execute("SAVEPOINT ${it.name}")
                }
                state.savepointStack.add(it)
            }
        }
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
                if (useNativeListeners) {
                    setTransactionListener(it)
                }
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
            if (state.outerSuccessful) {
                commit()
            } else {
                rollbackQuietly()
            }
        } finally {
            if (useNativeListeners && state.transactionListener != null) {
                execute(false) {
                    it.setTransactionListener(null)
                }
            }
            state.clear()
            release(permits)
        }
    }

    private fun commit() {
        execute(true) {
            runCatching {
                if (!useNativeListeners) {
                    state.transactionListener?.onCommit()
                }
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
                if (!useNativeListeners) {
                    state.transactionListener?.onRollback()
                }
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
