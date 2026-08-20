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

import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

internal class KeyHashingTest {
    @Test
    fun sameKeyProducesSameHashWithinProcess() {
        val keyOne = "exactly-32-bytes-of-key-data!!!!".toCharArray()
        val keyTwo = "exactly-32-bytes-of-key-data!!!!".toCharArray()
        assertEquals(hashKeyChars(keyOne), hashKeyChars(keyTwo))
    }

    @Test
    fun differentKeysProduceDifferentHashes() {
        val keyOne = "exactly-32-bytes-of-key-data!!!!".toCharArray()
        val keyTwo = "different-32-bytes-of-key-data!!".toCharArray()
        assertNotEquals(hashKeyChars(keyOne), hashKeyChars(keyTwo))
    }

    @Test
    fun hashIsNotBareShaOfTheKey() {
        val key = "exactly-32-bytes-of-key-data!!!!"
        val bareSha256 = MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        assertNotEquals(bareSha256, hashKeyChars(key.toCharArray()))
    }

    @Test
    fun sameKeyHashesDifferentlyAcrossProcesses() {
        val key = "exactly-32-bytes-of-key-data!!!!"
        val hashInThisProcess = hashKeyChars(key.toCharArray())
        val hashInOtherProcess = hashKeyCharsInSubprocess(key)
        assertNotEquals(hashInThisProcess, hashInOtherProcess)
    }

    private fun hashKeyCharsInSubprocess(key: String): String {
        val javaBin = "${System.getProperty("java.home")}${File.separator}bin${File.separator}java"
        val classpath = System.getProperty("java.class.path")
        val process = ProcessBuilder(
            javaBin,
            "-cp",
            classpath,
            "com.bloomberg.selekt.jdbc.driver.HashKeyCharsProcessMainKt"
        ).redirectErrorStream(true).start()
        process.outputStream.use {
            it.write("$key\n".toByteArray(Charsets.UTF_8))
            it.flush()
        }
        val output = process.inputStream.bufferedReader().readText()
        val exited = process.waitFor(30, TimeUnit.SECONDS)
        assertTrue(exited, "Subprocess did not exit in time: $output")
        assertEquals(0, process.exitValue(), "Subprocess failed: $output")
        return output.trim()
    }
}
