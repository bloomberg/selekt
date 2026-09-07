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

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ThrowingFfmCallbackTest {
    private companion object {
        const val LIBRARY_PATH_PROPERTY = "com.bloomberg.selekt.library_path"
    }

    @TestFactory
    fun `throwing callbacks never escape the native boundary`() = listOf(
        Scenario("commit", "runtime", "java.lang.IllegalStateException:callback runtime failure", "ROLLED_BACK"),
        Scenario("commit", "error", "java.lang.AssertionError:callback error failure", "ROLLED_BACK"),
        Scenario("rollback", "runtime", "java.lang.IllegalStateException:callback runtime failure", "ROLLED_BACK"),
        Scenario("rollback", "error", "java.lang.AssertionError:callback error failure", "ROLLED_BACK"),
        Scenario("progress", "runtime", "java.lang.IllegalStateException:callback runtime failure", "INTERRUPTED"),
        Scenario("progress", "error", "java.lang.AssertionError:callback error failure", "INTERRUPTED")
    ).map { scenario ->
        DynamicTest.dynamicTest(scenario.displayName) {
            runChild(scenario)
        }
    }

    private fun runChild(scenario: Scenario) {
        val java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
        val process = ProcessBuilder(
            java,
            "--enable-native-access=ALL-UNNAMED",
            "-D$LIBRARY_PATH_PROPERTY=${System.getProperty(LIBRARY_PATH_PROPERTY)}",
            "-cp",
            System.getProperty("java.class.path"),
            ThrowingFfmCallbackMain::class.java.name,
            scenario.callback,
            scenario.failureType
        ).redirectErrorStream(true).start()
        val exited = process.waitFor(30, TimeUnit.SECONDS)
        if (!exited) {
            process.destroyForcibly()
        }
        val output = process.inputStream.bufferedReader().use { it.readText() }
        assertTrue(exited, "Child JVM timed out: $output")
        assertEquals(0, process.exitValue(), "Child JVM terminated: $output")
        assertTrue("THROWABLE=${scenario.expectedThrowable}" in output, "Unexpected child throwable: $output")
        assertTrue("OUTCOME=${scenario.expectedOutcome}" in output, "Unexpected database outcome: $output")
    }

    private data class Scenario(
        val callback: String,
        val failureType: String,
        val expectedThrowable: String,
        val expectedOutcome: String
    ) {
        val displayName = "$callback callback contains $failureType"
    }
}
