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

package com.bloomberg.selekt.jdbc.driver

import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.externalSQLiteSingleton
import com.bloomberg.selekt.CommonThreadLocalRandom
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SharedDatabaseTest {
    private val databases = mutableListOf<SharedDatabase>()
    private val tempFiles = mutableListOf<File>()

    private fun createSharedDatabase(onClose: () -> Unit = {}): SharedDatabase {
        val tempFile = File.createTempFile("selekt-test-", ".db").also {
            it.deleteOnExit()
            tempFiles.add(it)
        }
        val db = SQLDatabase(
            path = tempFile.absolutePath,
            sqlite = object : com.bloomberg.selekt.SQLite(externalSQLiteSingleton()) {
                override fun throwSQLException(
                    code: com.bloomberg.selekt.SQLCode,
                    extendedCode: com.bloomberg.selekt.SQLCode,
                    message: String,
                    context: String?
                ): Nothing = throw RuntimeException(message)
            },
            configuration = SQLiteJournalMode.WAL.databaseConfiguration,
            key = null,
            random = CommonThreadLocalRandom
        )
        return SharedDatabase(db, onClose).also { databases.add(it) }
    }

    @AfterEach
    fun tearDown() {
        databases.forEach {
            while (it.isOpen()) {
                it.release()
            }
        }
    }

    @Test
    fun startsOpen(): Unit = createSharedDatabase().run {
        assertTrue(isOpen())
    }

    @Test
    fun releaseCloses(): Unit = createSharedDatabase().run {
        release()
        assertFalse(isOpen())
    }

    @Test
    fun retainPreventsClose(): Unit = createSharedDatabase().run {
        retain()
        release()
        assertTrue(isOpen())
    }

    @Test
    fun retainThenReleaseTwiceCloses(): Unit = createSharedDatabase().run {
        retain()
        release()
        release()
        assertFalse(isOpen())
    }

    @Test
    fun onCloseCalledOnRelease() {
        val called = AtomicBoolean(false)
        createSharedDatabase { called.set(true) }.run {
            release()
        }
        assertTrue(called.get())
    }

    @Test
    fun onCloseNotCalledWhileRetained() {
        val called = AtomicBoolean(false)
        createSharedDatabase { called.set(true) }.run {
            retain()
            release()
        }
        assertFalse(called.get())
    }

    @Test
    fun retainAfterReleaseThrows(): Unit = createSharedDatabase().run {
        release()
        assertFailsWith<IllegalStateException> {
            retain()
        }
    }

    @Test
    fun databaseIsAccessible(): Unit = createSharedDatabase().run {
        database.run {
            exec("CREATE TABLE t (x INTEGER)")
            exec("INSERT INTO t VALUES (42)")
        }
        release()
        assertFalse(isOpen())
    }
}
