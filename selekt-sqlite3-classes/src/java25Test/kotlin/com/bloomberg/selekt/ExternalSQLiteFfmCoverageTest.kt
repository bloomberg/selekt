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

import java.io.File
import java.lang.foreign.Arena
import java.lang.invoke.MethodHandle
import java.lang.reflect.InvocationTargetException
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ExternalSQLiteFfmCoverageTest {
    private companion object {
        const val SQL_OPEN_READWRITE_OR_CREATE = 6
        const val SQL_ROW = 100
    }

    @TempDir
    lateinit var tempDir: File

    private val sqlite = externalSQLiteSingleton()
    private val externalType = Class.forName("com.bloomberg.selekt.ExternalSQLite")

    @Test
    fun `configured singleton overload retains the singleton`() {
        assertSame(sqlite, externalSQLiteSingleton(SQLiteConfiguration(1)))
    }

    @Test
    fun `empty cursor window native result codes are translated`() {
        assertFailsWith<OutOfMemoryError> { emptyCursorWindow(-2) }
        assertFailsWith<OutOfMemoryError> { emptyCursorWindow(-3) }
        assertFailsWith<IllegalStateException> { emptyCursorWindow(-4) }
        assertFailsWith<IllegalArgumentException> { emptyCursorWindow(-5) }
        assertNull(emptyCursorWindow(0))
    }

    @Test
    fun `secret allocation pointer is validated`() {
        assertEquals(1L, invokeExternalKt("requireSecretPointer", 1L))
        assertFailsWith<OutOfMemoryError> { invokeExternalKt("requireSecretPointer", 0L) }
    }

    @Test
    fun `callback failure stack grows beyond its initial capacity`() {
        val type = Class.forName("com.bloomberg.selekt.ExternalSQLite\$CallbackFailureStack")
        val constructor = type.getDeclaredConstructor().apply { trySetAccessible() }
        val enter = type.getDeclaredMethod("enter").apply { trySetAccessible() }
        val leave = type.getDeclaredMethod("leave", Int::class.javaPrimitiveType).apply { trySetAccessible() }
        val stack = constructor.newInstance()
        val scopes = List(5) { enter.invoke(stack) as Int }

        scopes.asReversed().forEach { assertNull(leave.invoke(stack, it)) }
    }

    @Test
    fun `statement segments accept explicit and absent attachments`() = withStatement { statement ->
        assertEquals(SQL_ROW, sqlite.step(StatementHandle(statement)))
        assertEquals(SQL_OK, sqlite.reset(statement))
        assertEquals(SQL_ROW, sqlite.step(StatementHandle(statement, java.lang.foreign.MemorySegment.ofAddress(statement))))
    }

    @Test
    fun `UTF8 allocation is released when a closed statement segment rejects a bind`() = withStatement { statement ->
        val arena = Arena.ofConfined()
        val closedSegment = arena.allocate(1)
        arena.close()

        assertFailsWith<IllegalStateException> {
            sqlite.bindText(StatementHandle(statement, closedSegment), 1, "café")
        }
    }

    @Test
    fun `SQLite heap pressure uses the transient UTF8 fallback`() = withStatement { statement ->
        val hardLimit = externalType.getDeclaredField("sqlite3_hard_heap_limit64").run {
            trySetAccessible()
            get(null) as MethodHandle
        }
        hardLimit.invokeWithArguments(1L)
        try {
            sqlite.bindText(statement, 1, "café")
        } finally {
            hardLimit.invokeWithArguments(0L)
        }
    }

    @Test
    fun `null SQL pointer decodes as an empty string`() {
        assertEquals("", sqlite.sql(0L))
    }

    @Test
    fun `cursor ownership failures release or reject native buffers`() = withStatement { statement ->
        val registry = mock<CursorWindowOwnershipRegistry>()
        val subject = newExternalSQLite(registry)
        whenever(registry.register(any())).thenThrow(IllegalStateException("registration failed"))
        assertFailsWith<IllegalStateException> {
            (subject as INativeCursorWindowSQLite).fillCursorWindow(statement, 0, 1, false)
        }

        val unowned = ByteBuffer.allocateDirect(8)
        whenever(registry.consume(unowned)).thenReturn(unowned)
        val failure = assertFailsWith<IllegalStateException> {
            (subject as INativeCursorWindowSQLite).freeCursorWindow(unowned)
        }
        assertTrue(failure.message.orEmpty().contains("ownership"))
    }

    private fun emptyCursorWindow(result: Long): ByteBuffer? = try {
        invokeExternalKt("emptyCursorWindow", result) as ByteBuffer?
    } catch (failure: InvocationTargetException) {
        throw checkNotNull(failure.cause)
    }

    private fun invokeExternalKt(name: String, argument: Long): Any? = try {
        Class.forName("com.bloomberg.selekt.ExternalSQLiteKt")
            .getDeclaredMethod(name, Long::class.javaPrimitiveType)
            .invoke(null, argument)
    } catch (failure: InvocationTargetException) {
        throw checkNotNull(failure.cause)
    }

    private fun newExternalSQLite(registry: CursorWindowOwnershipRegistry): IExternalSQLite =
        externalType.getDeclaredConstructor(
            SQLiteConfiguration::class.java,
            Function0::class.java,
            CursorWindowOwnershipRegistry::class.java
        ).apply { trySetAccessible() }
            .newInstance(SQLiteConfiguration(), { Unit }, registry) as IExternalSQLite

    private inline fun withStatement(block: (Long) -> Unit) {
        val dbHolder = LongArray(1)
        assertEquals(
            SQL_OK,
            sqlite.openV2(File(tempDir, "coverage.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        )
        val statementHolder = LongArray(1)
        assertEquals(SQL_OK, sqlite.prepareV2(dbHolder.single(), "SELECT 1", 8, statementHolder))
        try {
            block(statementHolder.single())
        } finally {
            sqlite.finalize(statementHolder.single())
            sqlite.closeV2(dbHolder.single())
        }
    }
}
