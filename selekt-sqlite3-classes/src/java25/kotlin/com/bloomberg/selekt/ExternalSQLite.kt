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

package com.bloomberg.selekt

import com.bloomberg.selekt.commons.forEachByPosition
import com.bloomberg.selekt.commons.loadLibrary
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.foreign.ValueLayout.JAVA_DOUBLE
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

fun externalSQLiteSingleton() = externalSQLiteSingleton(SQLiteConfiguration())

fun externalSQLiteSingleton(
    configuration: SQLiteConfiguration = SQLiteConfiguration()
) = ExternalSQLite.Singleton(configuration) {}

@Suppress("NOTHING_TO_INLINE")
private inline fun MemorySegment.getConfinedString(): String = Arena.ofConfined().use { arena ->
    if (address() == 0L) {
        ""
    } else {
        reinterpret(Long.MAX_VALUE, arena, null).getString(0)
    }
}

@Suppress("Detekt.LongParameterList", "Detekt.TooManyFunctions")
internal class ExternalSQLite(
    configuration: SQLiteConfiguration,
    loader: () -> Unit
) : IExternalSQLite {
    private val callbackArena = Arena.ofShared()
    private val activeListeners = mutableMapOf<Long, Pair<MemorySegment, MemorySegment>>()
    private val activeProgressHandlers = mutableMapOf<Long, MemorySegment>()

    init {
        loader()
        nativeInit(configuration.softHeapLimit)
    }

    internal object Singleton {
        private val lock = ReentrantLock()

        @Volatile
        private var instance: IExternalSQLite? = null

        operator fun invoke(
            configuration: SQLiteConfiguration,
            loader: () -> Unit
        ): IExternalSQLite = instance ?: lock.withLock {
            instance ?: ExternalSQLite(configuration, loader).also { instance = it }
        }
    }

    override fun bindBlob(
        statement: Long,
        index: Int,
        blob: ByteArray,
        length: Int
    ): SQLCode = Arena.ofConfined().use {
        sqlite3_bind_blob.invoke(
            MemorySegment.ofAddress(statement),
            index,
            MemorySegment.ofArray(blob),
            length,
            sqliteTransient
        ) as Int
    }

    override fun bindDouble(
        statement: Long,
        index: Int,
        value: Double
    ): SQLCode = sqlite3_bind_double.invoke(
        MemorySegment.ofAddress(statement),
        index,
        value
    ) as Int

    override fun bindInt(
        statement: Long,
        index: Int,
        value: Int
    ): SQLCode = sqlite3_bind_int.invoke(
        MemorySegment.ofAddress(statement),
        index,
        value
    ) as Int

    override fun bindInt64(
        statement: Long,
        index: Int,
        value: Long
    ): SQLCode = sqlite3_bind_int64.invoke(
        MemorySegment.ofAddress(statement),
        index,
        value
    ) as Int

    override fun bindNull(
        statement: Long,
        index: Int
    ): SQLCode = sqlite3_bind_null.invoke(
        MemorySegment.ofAddress(statement),
        index
    ) as Int

    override fun bindParameterCount(
        statement: Long
    ): Int = sqlite3_bind_parameter_count.invoke(MemorySegment.ofAddress(statement)) as Int

    override fun bindParameterIndex(
        statement: Long,
        name: String
    ): Int = Arena.ofConfined().use {
        sqlite3_bind_parameter_index.invoke(
            MemorySegment.ofAddress(statement),
            it.allocateFrom(name)
        ) as Int
    }

    override fun bindText(
        statement: Long,
        index: Int,
        value: String
    ): SQLCode = Arena.ofConfined().use {
        sqlite3_bind_text.invoke(
            MemorySegment.ofAddress(statement),
            index,
            it.allocateFrom(value),
            -1,
            sqliteTransient
        ) as Int
    }

    override fun bindZeroBlob(
        statement: Long,
        index: Int,
        length: Int
    ): SQLCode = sqlite3_bind_zeroblob.invoke(
        MemorySegment.ofAddress(statement),
        index,
        length
    ) as Int

    override fun bindRow(statement: Long, args: Array<out Any?>): SQLCode {
        Arena.ofConfined().use { arena ->
            val segment = MemorySegment.ofAddress(statement)
            args.forEachByPosition { arg, position ->
                val result = when (arg) {
                    is String -> sqlite3_bind_text.invoke(
                        segment, position, arena.allocateFrom(arg), -1, sqliteTransient
                    )
                    is Int -> sqlite3_bind_int.invoke(segment, position, arg)
                    null -> sqlite3_bind_null.invoke(segment, position)
                    is Long -> sqlite3_bind_int64.invoke(segment, position, arg)
                    is Double -> sqlite3_bind_double.invoke(segment, position, arg)
                    is ByteArray -> sqlite3_bind_blob.invoke(
                        segment, position, MemorySegment.ofArray(arg), arg.size, sqliteTransient
                    )
                    else -> throw IllegalArgumentException(
                        "Cannot bind arg of class ${arg.javaClass} at position $position.")
                } as Int
                if (result != SQL_OK) {
                    return result
                }
            }
        }
        return SQL_OK
    }

    override fun blobBytes(
        blob: Long
    ): Int = sqlite3_blob_bytes.invoke(MemorySegment.ofAddress(blob)) as Int

    override fun blobClose(
        blob: Long
    ): SQLCode = sqlite3_blob_close.invoke(MemorySegment.ofAddress(blob)) as Int

    override fun blobOpen(
        db: Long,
        name: String,
        table: String,
        column: String,
        row: Long,
        flags: Int,
        holder: LongArray
    ): SQLCode = Arena.ofConfined().use {
        val nameSegment = it.allocateFrom(name)
        val tableSegment = it.allocateFrom(table)
        val columnSegment = it.allocateFrom(column)
        val blobPointer = it.allocate(ADDRESS)
        val result = sqlite3_blob_open.invoke(
            MemorySegment.ofAddress(db),
            nameSegment,
            tableSegment,
            columnSegment,
            row,
            flags,
            blobPointer
        ) as Int
        if (result == 0) {
            holder[0] = blobPointer.get(ADDRESS, 0).address()
        }
        result
    }

    override fun blobRead(
        blob: Long,
        offset: Int,
        destination: ByteArray,
        destinationOffset: Int,
        length: Int
    ): SQLCode = Arena.ofConfined().use {
        val buffer = it.allocate(length.toLong())
        val result = sqlite3_blob_read.invoke(
            MemorySegment.ofAddress(blob),
            buffer,
            length,
            offset
        ) as Int
        if (result == 0) {
            MemorySegment.copy(buffer, JAVA_BYTE, 0, destination, destinationOffset, length)
        }
        result
    }

    override fun blobReopen(
        blob: Long,
        row: Long
    ): SQLCode = sqlite3_blob_reopen.invoke(
        MemorySegment.ofAddress(blob),
        row
    ) as Int

    override fun blobWrite(
        blob: Long,
        offset: Int,
        source: ByteArray,
        sourceOffset: Int,
        length: Int
    ): SQLCode = Arena.ofConfined().use {
        val buffer = it.allocate(length.toLong())
        MemorySegment.copy(source, sourceOffset, buffer, JAVA_BYTE, 0, length)
        sqlite3_blob_write.invoke(
            MemorySegment.ofAddress(blob),
            buffer,
            length,
            offset
        ) as Int
    }

    override fun busyTimeout(
        db: Long,
        millis: Int
    ): SQLCode = sqlite3_busy_timeout.invoke(
        MemorySegment.ofAddress(db),
        millis
    ) as Int

    override fun changes(
        db: Long
    ): Int = sqlite3_changes.invoke(MemorySegment.ofAddress(db)) as Int

    override fun clearBindings(
        statement: Long
    ): SQLCode = sqlite3_clear_bindings.invoke(MemorySegment.ofAddress(statement)) as Int

    override fun closeV2(
        db: Long
    ): SQLCode = sqlite3_close_v2.invoke(MemorySegment.ofAddress(db)) as Int

    override fun columnBlob(
        statement: Long,
        index: Int
    ): ByteArray? {
        val blob = sqlite3_column_blob.invoke(
            MemorySegment.ofAddress(statement),
            index
        ) as MemorySegment
        if (blob.address() == 0L) {
            return null
        }
        val size = sqlite3_column_bytes.invoke(
            MemorySegment.ofAddress(statement),
            index
        ) as Int
        if (size == 0) {
            return ByteArray(0)
        }
        return blob.reinterpret(size.toLong()).toArray(JAVA_BYTE)
    }

    override fun columnCount(
        statement: Long
    ): Int = sqlite3_column_count.invoke(MemorySegment.ofAddress(statement)) as Int

    override fun columnDouble(
        statement: Long,
        index: Int
    ): Double = sqlite3_column_double.invoke(
        MemorySegment.ofAddress(statement),
        index
    ) as Double

    override fun columnInt(
        statement: Long,
        index: Int
    ): Int = sqlite3_column_int.invoke(
        MemorySegment.ofAddress(statement),
        index
    ) as Int

    override fun columnInt64(
        statement: Long,
        index: Int
    ): Long = sqlite3_column_int64.invoke(
        MemorySegment.ofAddress(statement),
        index
    ) as Long

    override fun columnName(
        statement: Long,
        index: Int
    ): String = (sqlite3_column_name.invoke(
        MemorySegment.ofAddress(statement),
        index
    ) as MemorySegment).run(MemorySegment::getConfinedString)

    override fun columnText(
        statement: Long,
        index: Int
    ): String = (sqlite3_column_text.invoke(
        MemorySegment.ofAddress(statement),
        index
    ) as MemorySegment).run(MemorySegment::getConfinedString)

    override fun columnType(
        statement: Long,
        index: Int
    ): SQLDataType = sqlite3_column_type.invoke(
        MemorySegment.ofAddress(statement),
        index
    ) as Int

    override fun columnValue(
        statement: Long,
        index: Int
    ): Long = (sqlite3_column_value.invoke(
        MemorySegment.ofAddress(statement),
        index
    ) as MemorySegment).address()

    override fun commitHook(
        db: Long,
        enabled: Boolean,
        listener: SQLCommitListener?
    ): SQLCode {
        val segment = MemorySegment.ofAddress(db)
        if (enabled && listener != null) {
            val commitStub = createCommitHookStub(listener)
            val rollbackStub = createRollbackHookStub(listener)
            sqlite3_commit_hook.invoke(segment, commitStub, MemorySegment.NULL)
            sqlite3_rollback_hook.invoke(segment, rollbackStub, MemorySegment.NULL)
            activeListeners[db] = Pair(commitStub, rollbackStub)
        } else {
            sqlite3_commit_hook.invoke(segment, MemorySegment.NULL, MemorySegment.NULL)
            sqlite3_rollback_hook.invoke(segment, MemorySegment.NULL, MemorySegment.NULL)
            activeListeners.remove(db)
        }
        return SQL_OK
    }

    override fun databaseConfig(
        db: Long,
        op: Int,
        value: Int
    ): Int = sqlite3_db_config.invoke(
        MemorySegment.ofAddress(db),
        op,
        value,
        MemorySegment.NULL
    ) as Int

    override fun databaseHandle(
        statement: Long
    ): Long = (sqlite3_db_handle.invoke(
        MemorySegment.ofAddress(statement)
    ) as MemorySegment).address()

    override fun databaseReadOnly(
        db: Long,
        name: String
    ): Int = Arena.ofConfined().use {
        sqlite3_db_readonly.invoke(
            MemorySegment.ofAddress(db),
            it.allocateFrom(name)
        ) as Int
    }

    override fun databaseReleaseMemory(
        db: Long
    ): Int = sqlite3_db_release_memory.invoke(MemorySegment.ofAddress(db)) as Int

    override fun databaseStatus(
        db: Long,
        options: Int,
        reset: Boolean,
        holder: IntArray
    ): SQLCode = Arena.ofConfined().use { arena ->
        val current = arena.allocate(JAVA_INT)
        val highwater = arena.allocate(JAVA_INT)
        (sqlite3_db_status.invoke(
            MemorySegment.ofAddress(db),
            options,
            current,
            highwater,
            if (reset) 1 else 0
        ) as Int).also {
            if (it == 0 && holder.size >= 2) {
                holder[0] = current.get(JAVA_INT, 0)
                holder[1] = highwater.get(JAVA_INT, 0)
            }
        }
    }

    override fun errorCode(
        db: Long
    ): Int = sqlite3_errcode.invoke(MemorySegment.ofAddress(db)) as Int

    override fun errorMessage(
        db: Long
    ): String = (sqlite3_errmsg.invoke(MemorySegment.ofAddress(db)) as MemorySegment)
        .run(MemorySegment::getConfinedString)

    override fun exec(
        db: Long,
        query: String
    ): SQLCode = Arena.ofConfined().use {
        sqlite3_exec.invoke(
            MemorySegment.ofAddress(db),
            it.allocateFrom(query),
            MemorySegment.NULL,
            MemorySegment.NULL,
            MemorySegment.NULL
        ) as Int
    }

    override fun expandedSql(
        statement: Long
    ): String = (sqlite3_expanded_sql.invoke(MemorySegment.ofAddress(statement)) as MemorySegment)
        .run(MemorySegment::getConfinedString)

    override fun extendedErrorCode(
        db: Long
    ): Int = sqlite3_extended_errcode.invoke(MemorySegment.ofAddress(db)) as Int

    override fun extendedResultCodes(
        db: Long,
        onOff: Int
    ): Int = sqlite3_extended_result_codes.invoke(
        MemorySegment.ofAddress(db),
        onOff
    ) as Int

    override fun finalize(
        statement: Long
    ): SQLCode = sqlite3_finalize.invoke(MemorySegment.ofAddress(statement)) as Int

    override fun getAutocommit(
        db: Long
    ): Int = sqlite3_get_autocommit.invoke(MemorySegment.ofAddress(db)) as Int

    external override fun gitCommit(): String

    override fun hardHeapLimit64(): Long = sqlite3_hard_heap_limit64.invoke(-1L) as Long

    override fun interrupt(
        db: Long
    ) {
        sqlite3_interrupt.invoke(MemorySegment.ofAddress(db))
    }

    override fun isInterrupted(
        db: Long
    ): Int = sqlite3_is_interrupted.invoke(MemorySegment.ofAddress(db)) as Int

    override fun key(
        db: Long,
        key: ByteArray,
        length: Int
    ): SQLCode = Arena.ofConfined().use {
        sqlite3_key.invoke(
            MemorySegment.ofAddress(db),
            it.allocateFrom(JAVA_BYTE, *key),
            length
        ) as Int
    }

    override fun keyConventionally(
        db: Long,
        key: ByteArray,
        length: Int
    ): SQLCode = this.key(db, key, length)

    override fun keywordCount(): Int = sqlite3_keyword_count.invoke() as Int

    override fun lastInsertRowId(
        db: Long
    ): Long = sqlite3_last_insert_rowid.invoke(MemorySegment.ofAddress(db)) as Long

    override fun libVersion(): String = (sqlite3_libversion.invoke() as MemorySegment)
        .run(MemorySegment::getConfinedString)

    override fun libVersionNumber(): Int = sqlite3_libversion_number.invoke() as Int

    override fun memoryUsed(): Long = sqlite3_memory_used.invoke() as Long

    override fun openV2(
        path: String,
        flags: Int,
        dbHolder: LongArray
    ): SQLCode = Arena.ofConfined().use {
        val pathSegment = it.allocateFrom(path)
        val dbPointer = it.allocate(ADDRESS)
        val result = sqlite3_open_v2.invoke(
            pathSegment,
            dbPointer,
            flags,
            MemorySegment.NULL
        ) as Int
        if (result == 0) {
            dbHolder[0] = dbPointer.get(ADDRESS, 0).address()
        }
        result
    }

    override fun prepareV2(
        db: Long,
        sql: String,
        length: Int,
        statementHolder: LongArray
    ): SQLCode = Arena.ofConfined().use {
        val statement = it.allocate(ADDRESS)
        val result = sqlite3_prepare_v2.invoke(
            MemorySegment.ofAddress(db),
            it.allocateFrom(sql),
            length,
            statement,
            MemorySegment.NULL
        ) as Int
        if (result == 0) {
            statementHolder[0] = statement.get(ADDRESS, 0).address()
        }
        result
    }

    override fun progressHandler(
        db: Long,
        instructionCount: Int,
        handler: SQLProgressHandler?
    ) {
        val segment = MemorySegment.ofAddress(db)
        if (handler != null) {
            val stub = createProgressHandlerStub(handler)
            sqlite3_progress_handler.invoke(segment, instructionCount, stub, MemorySegment.NULL)
            activeProgressHandlers[db] = stub
        } else {
            sqlite3_progress_handler.invoke(segment, 0, MemorySegment.NULL, MemorySegment.NULL)
            activeProgressHandlers.remove(db)
        }
    }

    override fun rawKey(
        db: Long,
        key: ByteArray,
        length: Int
    ): SQLCode = this.key(db, key, length)

    override fun rekey(
        db: Long,
        key: ByteArray,
        length: Int
    ): SQLCode = Arena.ofConfined().use {
        sqlite3_rekey.invoke(
            MemorySegment.ofAddress(db),
            it.allocateFrom(JAVA_BYTE, *key),
            length
        ) as Int
    }

    override fun releaseMemory(
        bytes: Int
    ): Int = sqlite3_release_memory.invoke(bytes) as Int

    override fun reset(
        statement: Long
    ): SQLCode = sqlite3_reset.invoke(MemorySegment.ofAddress(statement)) as Int

    override fun resetAndClearBindings(statement: Long): SQLCode {
        reset(statement)
        return clearBindings(statement)
    }

    external override fun softHeapLimit64(): Long

    override fun sql(statement: Long): String = (sqlite3_sql.invoke(MemorySegment.ofAddress(statement)) as MemorySegment)
        .run(MemorySegment::getConfinedString)

    override fun statementBusy(
        statement: Long
    ): Int = sqlite3_stmt_busy.invoke(MemorySegment.ofAddress(statement)) as Int

    override fun statementReadOnly(
        statement: Long
    ): Int = sqlite3_stmt_readonly.invoke(MemorySegment.ofAddress(statement)) as Int

    override fun statementStatus(
        statement: Long,
        options: Int,
        reset: Boolean
    ): Int = sqlite3_stmt_status.invoke(
        MemorySegment.ofAddress(statement),
        options,
        if (reset) 1 else 0
    ) as Int

    override fun step(
        statement: Long
    ): SQLCode = sqlite3_step.invoke(MemorySegment.ofAddress(statement)) as Int

    override fun threadsafe(): Int = sqlite3_threadsafe.invoke() as Int

    override fun totalChanges(
        db: Long
    ): Int = sqlite3_total_changes.invoke(MemorySegment.ofAddress(db)) as Int

    override fun traceV2(
        db: Long,
        flag: Int
    ) {
        sqlite3_trace_v2.invoke(
            MemorySegment.ofAddress(db),
            flag,
            MemorySegment.NULL,
            MemorySegment.NULL
        )
    }

    override fun transactionState(
        db: Long
    ): Int = sqlite3_txn_state.invoke(
        MemorySegment.ofAddress(db),
        MemorySegment.NULL
    ) as Int

    override fun valueDup(
        value: Long
    ): Long = (sqlite3_value_dup.invoke(MemorySegment.ofAddress(value)) as MemorySegment).address()

    override fun valueFree(value: Long) {
        sqlite3_value_free.invoke(MemorySegment.ofAddress(value))
    }

    override fun valueFromBind(
        value: Long
    ): Int = sqlite3_value_frombind.invoke(MemorySegment.ofAddress(value)) as Int

    override fun walAutoCheckpoint(
        db: Long,
        pages: Int
    ): SQLCode = sqlite3_wal_autocheckpoint.invoke(
        MemorySegment.ofAddress(db),
        pages
    ) as Int

    override fun walCheckpointV2(
        db: Long,
        name: String?,
        mode: Int
    ): SQLCode = Arena.ofConfined().use {
        sqlite3_wal_checkpoint_v2.invoke(
            MemorySegment.ofAddress(db),
            if (name != null) { it.allocateFrom(name) } else { MemorySegment.NULL },
            mode,
            MemorySegment.NULL,
            MemorySegment.NULL
        ) as Int
    }

    private fun createCommitHookStub(listener: SQLCommitListener): MemorySegment {
        val methodHandle = MethodHandles.lookup().findVirtual(
            SQLCommitListener::class.java,
            "onCommit",
            MethodType.methodType(Int::class.javaPrimitiveType)
        ).bindTo(listener)
        return linker.upcallStub(
            MethodHandles.dropArguments(methodHandle, 0, MemorySegment::class.java),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            callbackArena
        )
    }

    private fun createRollbackHookStub(listener: SQLCommitListener): MemorySegment {
        val methodHandle = MethodHandles.lookup().findVirtual(
            SQLCommitListener::class.java,
            "onRollback",
            MethodType.methodType(Void.TYPE)
        ).bindTo(listener)
        return linker.upcallStub(
            MethodHandles.dropArguments(methodHandle, 0, MemorySegment::class.java),
            FunctionDescriptor.ofVoid(ADDRESS),
            callbackArena
        )
    }

    private fun createProgressHandlerStub(handler: SQLProgressHandler): MemorySegment {
        val methodHandle = MethodHandles.lookup().findVirtual(
            SQLProgressHandler::class.java,
            "onProgress",
            MethodType.methodType(Int::class.javaPrimitiveType)
        ).bindTo(handler)
        return linker.upcallStub(
            MethodHandles.dropArguments(methodHandle, 0, MemorySegment::class.java),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            callbackArena
        )
    }

    private external fun nativeInit(softHeapLimit: Long)

    companion object {
        init {
            loadLibrary(checkNotNull(ExternalSQLite::class.java.classLoader), "jni", "selekt")
        }

        private val linker: Linker = Linker.nativeLinker()
        private val symbolLookup: SymbolLookup = SymbolLookup.loaderLookup()

        private val sqliteTransient = MemorySegment.ofAddress(-1L)

        private val criticalOption = Linker.Option.critical(true)

        private val sqlite3_bind_blob: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_bind_blob").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_bind_double: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_bind_double").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_DOUBLE),
            criticalOption
        )
        private val sqlite3_bind_int: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_bind_int").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT),
            criticalOption
        )
        private val sqlite3_bind_int64: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_bind_int64").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_LONG),
            criticalOption
        )
        private val sqlite3_bind_null: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_bind_null").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_bind_parameter_count: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_bind_parameter_count").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_bind_parameter_index: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_bind_parameter_index").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS),
            criticalOption
        )
        private val sqlite3_bind_text: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_bind_text").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_bind_zeroblob: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_bind_zeroblob").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT),
            criticalOption
        )
        private val sqlite3_blob_bytes: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_blob_bytes").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_blob_close: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_blob_close").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_blob_open: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_blob_open").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS, JAVA_LONG, JAVA_INT, ADDRESS)
        )
        private val sqlite3_blob_read: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_blob_read").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT)
        )
        private val sqlite3_blob_reopen: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_blob_reopen").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_LONG),
            criticalOption
        )
        private val sqlite3_blob_write: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_blob_write").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT)
        )
        private val sqlite3_busy_timeout: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_busy_timeout").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_changes: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_changes").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_clear_bindings: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_clear_bindings").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_close_v2: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_close_v2").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS)
        )
        private val sqlite3_column_blob: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_column_blob").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_column_bytes: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_column_bytes").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_column_count: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_column_count").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_column_double: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_column_double").orElseThrow(),
            FunctionDescriptor.of(JAVA_DOUBLE, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_column_int: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_column_int").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_column_int64: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_column_int64").orElseThrow(),
            FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_column_name: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_column_name").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_column_text: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_column_text").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_column_type: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_column_type").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_column_value: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_column_value").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_commit_hook: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_commit_hook").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, ADDRESS),
            criticalOption
        )
        private val sqlite3_db_config: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_db_config").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, ADDRESS)
        )
        private val sqlite3_db_handle: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_db_handle").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, ADDRESS),
            criticalOption
        )
        private val sqlite3_db_readonly: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_db_readonly").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS),
            criticalOption
        )
        private val sqlite3_db_release_memory: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_db_release_memory").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_db_status: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_db_status").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_errcode: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_errcode").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_errmsg: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_errmsg").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, ADDRESS),
            criticalOption
        )
        private val sqlite3_exec: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_exec").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS)
        )
        private val sqlite3_expanded_sql: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_expanded_sql").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, ADDRESS),
            criticalOption
        )
        private val sqlite3_extended_errcode: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_extended_errcode").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_extended_result_codes: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_extended_result_codes").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_finalize: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_finalize").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS)
        )
        private val sqlite3_get_autocommit: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_get_autocommit").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_hard_heap_limit64: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_hard_heap_limit64").orElseThrow(),
            FunctionDescriptor.of(JAVA_LONG, JAVA_LONG),
            criticalOption
        )
        private val sqlite3_interrupt: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_interrupt").orElseThrow(),
            FunctionDescriptor.ofVoid(ADDRESS),
            criticalOption
        )
        private val sqlite3_is_interrupted: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_is_interrupted").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_key: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_key").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_keyword_count: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_keyword_count").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT),
            criticalOption
        )
        private val sqlite3_last_insert_rowid: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_last_insert_rowid").orElseThrow(),
            FunctionDescriptor.of(JAVA_LONG, ADDRESS),
            criticalOption
        )
        private val sqlite3_libversion: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_libversion").orElseThrow(),
            FunctionDescriptor.of(ADDRESS),
            criticalOption
        )
        private val sqlite3_libversion_number: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_libversion_number").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT),
            criticalOption
        )
        private val sqlite3_memory_used: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_memory_used").orElseThrow(),
            FunctionDescriptor.of(JAVA_LONG),
            criticalOption
        )
        private val sqlite3_open_v2: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_open_v2").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, ADDRESS)
        )
        private val sqlite3_prepare_v2: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_prepare_v2").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, ADDRESS, ADDRESS),
            criticalOption
        )
        private val sqlite3_progress_handler: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_progress_handler").orElseThrow(),
            FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS, ADDRESS)
        )
        private val sqlite3_rekey: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_rekey").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_release_memory: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_release_memory").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, JAVA_INT),
            criticalOption
        )
        private val sqlite3_reset: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_reset").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS)
        )
        private val sqlite3_rollback_hook: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_rollback_hook").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, ADDRESS),
            criticalOption
        )
        private val sqlite3_sql: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_sql").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, ADDRESS),
            criticalOption
        )
        private val sqlite3_step: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_step").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS)
        )
        private val sqlite3_stmt_busy: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_stmt_busy").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_stmt_readonly: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_stmt_readonly").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_stmt_status: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_stmt_status").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT),
            criticalOption
        )
        private val sqlite3_threadsafe: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_threadsafe").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT),
            criticalOption
        )
        private val sqlite3_total_changes: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_total_changes").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_trace_v2: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_trace_v2").orElseThrow(),
            FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS, ADDRESS),
            criticalOption
        )
        private val sqlite3_txn_state: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_txn_state").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS),
            criticalOption
        )
        private val sqlite3_value_dup: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_value_dup").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, ADDRESS),
            criticalOption
        )
        private val sqlite3_value_free: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_value_free").orElseThrow(),
            FunctionDescriptor.ofVoid(ADDRESS),
            criticalOption
        )
        private val sqlite3_value_frombind: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_value_frombind").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS),
            criticalOption
        )
        private val sqlite3_wal_autocheckpoint: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_wal_autocheckpoint").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT),
            criticalOption
        )
        private val sqlite3_wal_checkpoint_v2: MethodHandle = linker.downcallHandle(
            symbolLookup.find("sqlite3_wal_checkpoint_v2").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, ADDRESS, ADDRESS),
            criticalOption
        )
    }
}
