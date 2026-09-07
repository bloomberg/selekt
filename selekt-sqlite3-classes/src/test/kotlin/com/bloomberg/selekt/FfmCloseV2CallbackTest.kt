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
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

private const val SQL_OPEN_READWRITE = 2
private const val SQL_OPEN_CREATE = 4

internal class FfmCloseV2CallbackTest {
    @Test
    fun `closeV2 detaches commit hook before closing its arena`() = runProbe("commit")

    @Test
    fun `closeV2 detaches rollback hook before closing its arena`() = runProbe("rollback")

    @Test
    fun `closeV2 detaches progress handler before closing its arena`() = runProbe("progress")

    private fun runProbe(mode: String) {
        assumeTrue(Runtime.version().feature() >= 25)
        val command = mutableListOf(
            Path.of(System.getProperty("java.home"), "bin", "java").toString(),
            "--enable-native-access=ALL-UNNAMED"
        )
        System.getProperty("com.bloomberg.selekt.library_path")?.let {
            command += "-Dcom.bloomberg.selekt.library_path=$it"
        }
        command += listOf(
            "-cp",
            System.getProperty("java.class.path"),
            FfmCloseV2CallbackTestProbeMain::class.java.name,
            mode
        )

        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        val completed = process.waitFor(30, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            process.waitFor()
        }
        assertTrue(completed, "FFM closeV2 callback probe timed out")
        val output = process.inputStream.bufferedReader().readText()
        assertEquals(0, process.exitValue(), "FFM closeV2 callback probe '$mode' failed:\n$output")
    }
}

internal object FfmCloseV2CallbackTestProbeMain {
    @JvmStatic
    fun main(args: Array<String>) {
        require(Runtime.version().feature() >= 25)
        require(args.size == 1)
        when (args.single()) {
            "commit" -> probeCommitHook()
            "rollback" -> probeRollbackHook()
            "progress" -> probeProgressHandler()
            else -> error("Unknown probe")
        }
    }

    private fun probeCommitHook() {
        withDatabase { sqlite, db ->
            check(sqlite.exec(db, "CREATE TABLE test (value INTEGER)") == SQL_OK)
            val statement = prepare(sqlite, db, "INSERT INTO test VALUES (1)")
            registerFailingCommitListener(sqlite, db)
            check(sqlite.closeV2(db) == SQL_OK)
            check(sqlite.step(statement) == SQL_DONE)
            check(sqlite.finalize(statement) == SQL_OK)
        }
    }

    private fun probeRollbackHook() {
        withDatabase { sqlite, db ->
            check(sqlite.exec(db, "CREATE TABLE test (value INTEGER)") == SQL_OK)
            check(sqlite.exec(db, "BEGIN; INSERT INTO test VALUES (1)") == SQL_OK)
            val statement = prepare(sqlite, db, "SELECT 1")
            registerFailingCommitListener(sqlite, db)
            check(sqlite.closeV2(db) == SQL_OK)
            check(sqlite.finalize(statement) == SQL_OK)
        }
    }

    private fun probeProgressHandler() {
        withDatabase { sqlite, db ->
            val statement = prepare(
                sqlite,
                db,
                """
                    WITH RECURSIVE counter(value) AS (
                        VALUES(0)
                        UNION ALL
                        SELECT value + 1 FROM counter WHERE value < 100
                    )
                    SELECT sum(value) FROM counter
                """.trimIndent()
            )
            sqlite.progressHandler(db, 1) {
                error("Progress handler remained registered after closeV2")
            }
            check(sqlite.closeV2(db) == SQL_OK)
            check(sqlite.step(statement) == SQL_ROW)
            check(sqlite.finalize(statement) == SQL_OK)
        }
    }

    private fun registerFailingCommitListener(sqlite: IExternalSQLite, db: Long) {
        val listener = object : SQLCommitListener {
            override fun onCommit(): Int = error("Commit hook remained registered after closeV2")

            override fun onRollback() {
                error("Rollback hook remained registered after closeV2")
            }
        }
        check(sqlite.commitHook(db, true, listener) == SQL_OK)
    }

    private fun prepare(sqlite: IExternalSQLite, db: Long, sql: String): Long {
        val statementHolder = LongArray(1)
        check(sqlite.prepareV2(db, sql, sql.length, statementHolder) == SQL_OK)
        return statementHolder[0]
    }

    private inline fun withDatabase(block: (IExternalSQLite, Long) -> Unit) {
        val sqlite = externalSQLiteSingleton()
        val dbHolder = LongArray(1)
        check(sqlite.openV2(":memory:", SQL_OPEN_READWRITE or SQL_OPEN_CREATE, dbHolder) == SQL_OK)
        block(sqlite, dbHolder[0])
    }
}
