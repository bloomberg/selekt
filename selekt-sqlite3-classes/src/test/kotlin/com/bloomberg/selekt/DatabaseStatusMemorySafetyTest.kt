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

import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

internal class DatabaseStatusMemorySafetyTest {
    @Test
    fun `databaseStatus rejects an empty holder without corrupting memory`() = runProbe(0)

    @Test
    fun `databaseStatus rejects a one-element holder without corrupting memory`() = runProbe(1)

    private fun runProbe(holderSize: Int) {
        val command = mutableListOf(
            Path.of(System.getProperty("java.home"), "bin", "java").toString()
        )
        if (Runtime.version().feature() >= 25) {
            command += "--enable-native-access=ALL-UNNAMED"
        }
        System.getProperty("com.bloomberg.selekt.library_path")?.let {
            command += "-Dcom.bloomberg.selekt.library_path=$it"
        }
        command += listOf(
            "-cp",
            System.getProperty("java.class.path"),
            DatabaseStatusMemorySafetyProbeMain::class.java.name,
            holderSize.toString()
        )
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        val completed = process.waitFor(30, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            process.waitFor()
        }
        assertTrue(completed, "Database-status probe timed out")
        val output = process.inputStream.bufferedReader().readText()
        assertEquals(0, process.exitValue(), "Database-status probe failed:\n$output")
    }
}

internal object DatabaseStatusMemorySafetyProbeMain {
    private const val SQL_OPEN_READWRITE_OR_CREATE = 6

    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size == 1)
        val holderSize = args.single().toInt()
        val sqlite = externalSQLiteSingleton()
        val databaseHolder = LongArray(1)
        check(sqlite.openV2(":memory:", SQL_OPEN_READWRITE_OR_CREATE, databaseHolder) == SQL_OK)
        try {
            val failure = runCatching {
                sqlite.databaseStatus(databaseHolder[0], 0, false, IntArray(holderSize))
            }.exceptionOrNull()
            check(failure is IndexOutOfBoundsException) {
                "Expected IndexOutOfBoundsException, got ${failure?.javaClass?.name ?: "no exception"}"
            }
        } finally {
            sqlite.closeV2(databaseHolder[0])
        }
    }
}
