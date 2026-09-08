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

private const val SECRET_SIZE = 32

internal class SecretMemorySafetyTest {
    @Test
    fun `freeSecret rejects a negative size without corrupting memory`() = runProbe("negative")

    @Test
    fun `freeSecret rejects a zero size without corrupting memory`() = runProbe("zero")

    @Test
    fun `freeSecret rejects a mismatched size without corrupting memory`() = runProbe("mismatched")

    private fun runProbe(mode: String) {
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
            SecretMemorySafetyProbeMain::class.java.name,
            mode
        )
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        val completed = process.waitFor(30, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            process.waitFor()
        }
        assertTrue(completed, "Secret-memory probe timed out")
        val output = process.inputStream.bufferedReader().readText()
        assertEquals(0, process.exitValue(), "Secret-memory probe '$mode' failed:\n$output")
    }
}

internal object SecretMemorySafetyProbeMain {
    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size == 1)
        val invalidSize = when (args.single()) {
            "negative" -> -1
            "zero" -> 0
            "mismatched" -> SECRET_SIZE + 1
            else -> error("Unknown probe")
        }
        val sqlite = externalSQLiteSingleton()
        val pointer = sqlite.allocateSecret(SECRET_SIZE)
        val failure = runCatching { sqlite.freeSecret(pointer, invalidSize) }.exceptionOrNull()
        check(failure is IllegalArgumentException) {
            "Expected IllegalArgumentException, got ${failure?.javaClass?.name ?: "no exception"}"
        }
        sqlite.freeSecret(pointer, SECRET_SIZE)
    }
}
