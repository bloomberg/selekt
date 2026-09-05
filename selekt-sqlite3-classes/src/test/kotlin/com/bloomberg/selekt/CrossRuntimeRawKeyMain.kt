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

import java.io.File

private const val SQL_OPEN_READWRITE = 2
private const val SQL_OPEN_CREATE = 4
private const val EXPECTED_VALUE = 42

fun main(args: Array<String>) {
    require(args.size == 2) { "Expected an operation and database path." }
    val operation = args[0]
    val path = args[1]
    if (operation == "write") {
        checkNotNull(File(path).parentFile).mkdirs()
        listOf(path, "$path-wal", "$path-shm").forEach { File(it).delete() }
    }

    val sqlite = externalSQLiteSingleton()
    val key = ByteArray(32) { (it + 1).toByte() }
    val dbHolder = LongArray(1)
    val flags = SQL_OPEN_READWRITE or if (operation == "write") { SQL_OPEN_CREATE } else { 0 }
    check(sqlite.openV2(path, flags, dbHolder) == SQL_OK) { "Failed to open cross-runtime test database." }
    val db = dbHolder[0]
    try {
        val keyPointer = sqlite.allocateSecret(key.size)
        try {
            sqlite.storeSecret(keyPointer, key.size, key, key.size)
            check(sqlite.keyConventionallyAt(db, keyPointer, key.size) == SQL_OK) {
                "Failed to apply raw key."
            }
        } finally {
            sqlite.freeSecret(keyPointer, key.size)
        }

        when (operation) {
            "write" -> {
                check(sqlite.exec(db, "CREATE TABLE compatibility_test(value INTEGER NOT NULL)") == SQL_OK)
                check(sqlite.exec(db, "INSERT INTO compatibility_test VALUES ($EXPECTED_VALUE)") == SQL_OK)
            }
            "read" -> verifyDatabaseContents(sqlite, db)
            else -> error("Unknown operation: $operation")
        }
    } finally {
        key.fill(0)
        check(sqlite.closeV2(db) == SQL_OK) { "Failed to close cross-runtime test database." }
    }
}

private fun verifyDatabaseContents(sqlite: IExternalSQLite, db: Long) {
    val statementHolder = LongArray(1)
    val sql = "SELECT value FROM compatibility_test"
    check(sqlite.prepareV2(db, sql, sql.length, statementHolder) == SQL_OK) {
        "Failed to prepare cross-runtime query."
    }
    val statement = statementHolder[0]
    try {
        check(sqlite.step(statement) == SQL_ROW) { "Cross-runtime database contained no test row." }
        check(sqlite.columnInt(statement, 0) == EXPECTED_VALUE) { "Cross-runtime database contained the wrong value." }
        check(sqlite.step(statement) == SQL_DONE) { "Cross-runtime database contained an unexpected extra row." }
    } finally {
        check(sqlite.finalize(statement) == SQL_OK) { "Failed to finalize cross-runtime query." }
    }
}
