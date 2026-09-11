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

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

private const val SECRET_SIZE = 32
private const val SQL_OPEN_READWRITE = 2
private const val SQL_OPEN_CREATE = 4
private const val SQL_OPEN_READWRITE_OR_CREATE = SQL_OPEN_READWRITE or SQL_OPEN_CREATE

internal class SecretMemorySafetyTest {
    @Test
    fun `freeSecret rejects a negative size without corrupting memory`() = runProbe("negative")

    @Test
    fun `freeSecret rejects a zero size without corrupting memory`() = runProbe("zero")

    @Test
    fun `freeSecret rejects a mismatched size without corrupting memory`() = runProbe("mismatched")

    @Test
    fun `storeSecret rejects an inflated capacity without corrupting memory`() = runProbe("store-capacity")

    @Test
    fun `pointer key APIs reject an undersized secret allocation`() = runProbe("key-undersized")

    @Test
    fun `pointer key APIs reject an oversized secret allocation`() = runProbe("key-oversized")

    @Test
    fun `pointer key APIs reject an unknown pointer`() = runProbe("key-unknown")

    @Test
    fun `pointer key APIs reject a freed pointer`() = runProbe("key-freed")

    @Test
    fun `pointer key APIs reject a null pointer for a non-empty key`() = runProbe("key-null")

    @Test
    fun `pointer key APIs reject invalid non-empty key lengths`() = runProbe("key-invalid-length")

    @Test
    fun `pointer key APIs accept a live exactly sized secret allocation`() = runProbe("key-valid")

    @Test
    fun `rekeyAt delegates a zero length null pointer without validating it`() = runProbe("rekey-empty")

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
        when (val mode = args.single()) {
            "store-capacity" -> probeInflatedStoreCapacity()
            "key-undersized" -> probeAllocationCapacity(1)
            "key-oversized" -> probeAllocationCapacity(SECRET_SIZE * 2)
            "key-unknown" -> probeInvalidKeyPointer(1L)
            "key-freed" -> probeFreedKeyPointer()
            "key-null" -> probeInvalidKeyPointer(0L)
            "key-invalid-length" -> probeInvalidKeyLength()
            "key-valid" -> probeValidKeyPointer()
            "rekey-empty" -> probeEmptyRekey()
            else -> probeInvalidFreeSize(mode)
        }
    }

    private fun probeInvalidFreeSize(mode: String) {
        val invalidSize = when (mode) {
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

    private fun probeInflatedStoreCapacity() {
        val sqlite = externalSQLiteSingleton()
        val pointer = sqlite.allocateSecret(1)
        val failure = runCatching {
            sqlite.storeSecret(pointer, SECRET_SIZE, ByteArray(SECRET_SIZE), SECRET_SIZE)
        }.exceptionOrNull()
        check(failure is IndexOutOfBoundsException) {
            "Expected IndexOutOfBoundsException, got ${failure?.javaClass?.name ?: "no exception"}"
        }
        sqlite.storeSecret(pointer, 1, byteArrayOf(0x5a), 1)
        sqlite.freeSecret(pointer, 1)
    }

    private fun probeAllocationCapacity(capacity: Int) {
        val sqlite = externalSQLiteSingleton()
        val pointer = sqlite.allocateSecret(capacity)
        try {
            assertPointerKeyConsumersReject(sqlite, pointer, SECRET_SIZE)
        } finally {
            // Rejection must leave a live allocation registered and freeable at its true capacity.
            sqlite.freeSecret(pointer, capacity)
        }
    }

    private fun probeInvalidKeyPointer(pointer: Long) {
        assertPointerKeyConsumersReject(externalSQLiteSingleton(), pointer, SECRET_SIZE)
    }

    private fun probeFreedKeyPointer() {
        val sqlite = externalSQLiteSingleton()
        val pointer = sqlite.allocateSecret(SECRET_SIZE)
        sqlite.freeSecret(pointer, SECRET_SIZE)
        assertPointerKeyConsumersReject(sqlite, pointer, SECRET_SIZE)
    }

    private fun probeInvalidKeyLength() {
        val sqlite = externalSQLiteSingleton()
        val pointer = sqlite.allocateSecret(1)
        try {
            assertPointerKeyConsumersReject(sqlite, pointer, 1)
        } finally {
            sqlite.freeSecret(pointer, 1)
        }
    }

    private fun probeValidKeyPointer() {
        val sqlite = externalSQLiteSingleton()
        val pointer = sqlite.allocateSecret(SECRET_SIZE)
        try {
            sqlite.storeSecret(pointer, SECRET_SIZE, ByteArray(SECRET_SIZE) { 0x5a }, SECRET_SIZE)
            withDatabase(sqlite) { db ->
                check(sqlite.keyConventionallyAt(db, pointer, SECRET_SIZE) == SQL_OK)
            }
            withDatabase(sqlite) { db ->
                check(sqlite.rawKeyAt(db, pointer, SECRET_SIZE) == SQL_OK)
            }
            withDatabase(sqlite) { db ->
                val initialKey = ByteArray(SECRET_SIZE) { 0x11 }
                check(sqlite.keyConventionally(db, initialKey, SECRET_SIZE) == SQL_OK)
                check(sqlite.exec(db, "CREATE TABLE keyed (value INTEGER)") == SQL_OK)
                check(sqlite.rekeyAt(db, pointer, SECRET_SIZE) == SQL_OK)
            }
        } finally {
            sqlite.freeSecret(pointer, SECRET_SIZE)
        }
    }

    private fun probeEmptyRekey() {
        val sqlite = externalSQLiteSingleton()
        withDatabase(sqlite) { db ->
            val initialKey = ByteArray(SECRET_SIZE) { 0x11 }
            check(sqlite.keyConventionally(db, initialKey, SECRET_SIZE) == SQL_OK)
            check(sqlite.exec(db, "CREATE TABLE keyed (value INTEGER)") == SQL_OK)
            sqlite.rekeyAt(db, 0L, 0)
        }
    }

    private fun assertPointerKeyConsumersReject(sqlite: IExternalSQLite, pointer: Long, length: Int) {
        withDatabase(sqlite) { db ->
            val consumers = listOf<(Long, Long, Int) -> SQLCode>(
                sqlite::keyConventionallyAt,
                sqlite::rawKeyAt,
                sqlite::rekeyAt
            )
            consumers.forEach { consumer ->
                val failure = runCatching { consumer(db, pointer, length) }.exceptionOrNull()
                check(failure is IllegalArgumentException) {
                    "Expected IllegalArgumentException, got ${failure?.javaClass?.name ?: "no exception"}"
                }
            }
        }
    }

    private inline fun withDatabase(sqlite: IExternalSQLite, block: (Long) -> Unit) {
        val path = Files.createTempFile("selekt-secret-memory-", ".db")
        val holder = LongArray(1)
        try {
            check(sqlite.openV2(path.toString(), SQL_OPEN_READWRITE_OR_CREATE, holder) == SQL_OK)
            try {
                block(holder.single())
            } finally {
                check(sqlite.closeV2(holder.single()) == SQL_OK)
            }
        } finally {
            Files.deleteIfExists(path)
        }
    }
}
