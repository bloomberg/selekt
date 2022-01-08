/*
 * Copyright 2022 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.commons

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DatabaseKtTest {
    private val parent = Files.createTempDirectory("foo").toFile().also { it.deleteOnExit() }

    @AfterEach
    fun tearDown() {
        assertTrue(parent.deleteRecursively())
    }

    @Test
    fun deleteDatabase() {
        arrayOf(
            "db",
            "db-journal",
            "db-shm",
            "db-wal"
        ).forEach {
            Files.createFile(File("${parent.absolutePath}/$it").toPath())
        }
        Files.createFile(File("${parent.absolutePath}/db-mj-bar").toPath())
        assertEquals(false, parent.listFiles { f -> f.name.startsWith("db") }?.isEmpty())
        deleteDatabase(File("${parent.absolutePath}/db"))
        assertEquals(true, parent.listFiles { f -> f.name.startsWith("db") }?.isEmpty())
    }

    @Test
    fun deleteDatabaseDirectory() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            deleteDatabase(parent)
        }
    }

    @Test
    fun deleteDatabaseWithoutParent() {
        mock<File>().apply {
            whenever(isFile) doReturn true
        }.let {
            assertDoesNotThrow {
                deleteDatabase(it)
            }
        }
    }

    @Test
    fun deleteDatabaseWithoutParentFile() {
        mock<File>().apply {
            whenever(isFile) doReturn true
            whenever(parentFile) doReturn File("foo")
        }.let {
            assertDoesNotThrow {
                deleteDatabase(it)
            }
        }
    }
}
