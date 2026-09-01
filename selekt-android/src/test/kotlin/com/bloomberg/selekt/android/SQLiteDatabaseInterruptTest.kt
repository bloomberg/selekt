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

package com.bloomberg.selekt.android

import android.content.ContentValues
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SQLiteDatabaseInterruptTest {
    private val database = SQLiteDatabase.createInMemoryDatabase()

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun isNotInterruptedByDefault(): Unit = database.run {
        assertFalse(isInterrupted)
    }

    @Test
    fun interruptSetsIsInterrupted(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)")
        interrupt()
        assertTrue(isInterrupted)
    }

    @Test
    fun interruptIdempotent(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)")
        interrupt()
        interrupt()
        assertTrue(isInterrupted)
    }

    @Test
    fun setProgressHandlerWithHandler(): Unit = database.run {
        var callCount = 0
        exec("CREATE TABLE 'Foo' (bar INT)")
        setProgressHandler(1) {
            callCount++
            0
        }
        exec("CREATE TABLE 'Bar' (baz INT)")
        assertTrue(callCount > 0)
    }

    @Test
    fun setProgressHandlerWithNull(): Unit = database.run {
        var callCount = 0
        setProgressHandler(1) {
            callCount++
            0
        }
        setProgressHandler(0, null)
        callCount = 0
        exec("CREATE TABLE 'Foo' (bar INT)")
        assertEquals(callCount, 0)
    }

    @Test
    fun progressHandlerCanInterruptOperation(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)")
        transact {
            repeat(100) { i ->
                insert("Foo", ContentValues().apply { put("bar", i) }, ConflictAlgorithm.REPLACE)
            }
        }
        setProgressHandler(1) { 1 }
        assertFails {
            query("SELECT * FROM 'Foo'", null).use { }
        }
    }

    @Test
    fun concurrentSetProgressHandlerDoesNotCorruptInFlightWrites(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)")
        val error = AtomicReference<Throwable?>()
        val stop = AtomicBoolean(false)
        val writer = thread(start = true) {
            try {
                var i = 0
                while (!stop.get()) {
                    transact {
                        insert("Foo", ContentValues().apply { put("bar", i++) }, ConflictAlgorithm.REPLACE)
                    }
                }
            } catch (e: Throwable) {
                error.set(e)
            }
        }
        try {
            repeat(500) {
                setProgressHandler(1) { 0 }
                setProgressHandler(0, null)
            }
        } finally {
            stop.set(true)
            writer.join(10_000L)
        }
        assertFalse(writer.isAlive)
        assertNull(error.get())
        query("SELECT COUNT(*) FROM 'Foo'", null).use { }
    }
}
