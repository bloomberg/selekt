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

internal object ThrowingFfmCallbackMain {
    private const val SQLITE_INTERRUPT = 9
    private const val SQLITE_OPEN_READWRITE_CREATE = 6
    private const val SQLITE_ROW = 100

    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size == 2) { "Expected callback and failure type" }
        val sqlite = externalSQLiteSingleton()
        val databaseHolder = LongArray(1)
        requireResult(SQL_OK, sqlite.openV2(":memory:", SQLITE_OPEN_READWRITE_CREATE, databaseHolder))
        val database = databaseHolder[0]
        try {
            val outcome = when (args[0]) {
                "commit" -> throwingCommit(sqlite, database, args[1])
                "rollback" -> throwingRollback(sqlite, database, args[1])
                "progress" -> throwingProgress(sqlite, database, args[1])
                else -> error("Unknown callback: ${args[0]}")
            }
            println(outcome)
        } finally {
            sqlite.progressHandler(database, 0, null)
            sqlite.commitHook(database, false, null)
            requireResult(SQL_OK, sqlite.closeV2(database))
        }
    }

    private fun throwingCommit(sqlite: IExternalSQLite, database: Long, failureType: String): String {
        requireResult(SQL_OK, sqlite.exec(database, "CREATE TABLE test(value INTEGER)"))
        requireResult(SQL_OK, sqlite.exec(database, "BEGIN; INSERT INTO test VALUES (1)"))
        val expected = failure(failureType)
        requireResult(SQL_OK, sqlite.commitHook(database, true, object : SQLCommitListener {
            override fun onCommit(): Int = throw expected

            override fun onRollback() = Unit
        }))
        val statement = prepare(sqlite, database, "COMMIT")
        try {
            assertExactThrowable(expected) {
                sqlite.step(sqlite.newStatementHandle(statement))
            }
        } finally {
            sqlite.finalize(statement)
        }
        sqlite.commitHook(database, false, null)
        check(queryCount(sqlite, database) == 0) { "Failed commit was not rolled back" }
        return outcome(expected, "ROLLED_BACK")
    }

    private fun throwingRollback(sqlite: IExternalSQLite, database: Long, failureType: String): String {
        requireResult(SQL_OK, sqlite.exec(database, "CREATE TABLE test(value INTEGER)"))
        val expected = failure(failureType)
        requireResult(SQL_OK, sqlite.commitHook(database, true, object : SQLCommitListener {
            override fun onCommit(): Int = 0

            override fun onRollback(): Unit = throw expected
        }))
        assertExactThrowable(expected) {
            sqlite.exec(database, "BEGIN; INSERT INTO test VALUES (1); ROLLBACK")
        }
        sqlite.commitHook(database, false, null)
        check(queryCount(sqlite, database) == 0) { "Rollback did not complete" }
        return outcome(expected, "ROLLED_BACK")
    }

    private fun throwingProgress(sqlite: IExternalSQLite, database: Long, failureType: String): String {
        val expected = failure(failureType)
        val statement = prepare(
            sqlite,
            database,
            """
                WITH RECURSIVE counter(value) AS (
                    VALUES(0)
                    UNION ALL
                    SELECT value + 1 FROM counter WHERE value < 100
                )
                SELECT sum(value) FROM counter
            """.trimIndent()
        )
        try {
            sqlite.progressHandler(database, 1) { throw expected }
            assertExactThrowable(expected) {
                (sqlite as INativeCursorWindowSQLite).fillCursorWindow(statement, 0, 100, false)
            }
            requireResult(SQLITE_INTERRUPT, sqlite.errorCode(database))
        } finally {
            sqlite.progressHandler(database, 0, null)
            sqlite.finalize(statement)
        }
        requireResult(SQL_OK, sqlite.exec(database, "SELECT 1"))
        return outcome(expected, "INTERRUPTED")
    }

    private fun failure(failureType: String): Throwable = when (failureType) {
        "runtime" -> IllegalStateException("callback runtime failure")
        "error" -> AssertionError("callback error failure")
        else -> error("Unknown failure type: $failureType")
    }

    private inline fun assertExactThrowable(expected: Throwable, block: () -> Unit) {
        val actual = try {
            block()
            null
        } catch (failure: Throwable) {
            failure
        }
        check(actual === expected) {
            "Expected the original ${expected.javaClass.name}, but received ${actual?.javaClass?.name}"
        }
    }

    private fun queryCount(sqlite: IExternalSQLite, database: Long): Int {
        val statement = prepare(sqlite, database, "SELECT count(*) FROM test")
        return try {
            requireResult(SQLITE_ROW, sqlite.step(statement))
            sqlite.columnInt(statement, 0)
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun prepare(sqlite: IExternalSQLite, database: Long, sql: String): Long {
        val statementHolder = LongArray(1)
        requireResult(SQL_OK, sqlite.prepareV2(database, sql, sql.length, statementHolder))
        return statementHolder[0]
    }

    private fun outcome(failure: Throwable, databaseOutcome: String) =
        "THROWABLE=${failure.javaClass.name}:${failure.message};OUTCOME=$databaseOutcome"

    private fun requireResult(expected: Int, actual: Int) {
        check(expected == actual) { "Expected SQLite result $expected but was $actual" }
    }
}
