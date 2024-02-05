/*
 * Copyright 2020 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.android

import com.bloomberg.selekt.commons.deleteDatabase
import com.bloomberg.selekt.NULL
import com.bloomberg.selekt.Pointer
import com.bloomberg.selekt.SQLOpenOperation
import com.bloomberg.selekt.SQL_DONE
import com.bloomberg.selekt.SQL_OK
import com.bloomberg.selekt.SQL_OPEN_CREATE
import com.bloomberg.selekt.SQL_OPEN_READWRITE
import com.bloomberg.selekt.SQL_ROW
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class SQLiteJson1Test {
    private val file = createTempFile("test-sqlite-json1", ".db").toFile().apply { deleteOnExit() }

    private var db: Pointer = NULL

    @BeforeEach
    fun setUp() {
        db = openConnection()
    }

    @AfterEach
    fun tearDown() {
        try {
            assertEquals(SQL_OK, SQLite.closeV2(db))
        } finally {
            if (file.exists()) {
                assertTrue(deleteDatabase(file))
            }
        }
    }

    @Test
    fun jsonEach() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE user (name TEXT, phone TEXT)"))
        SQLite.exec(db, "INSERT INTO user VALUES ('Mike', '[\"704-123\", \"704-456\"]')")
        prepareStatement(
            db,
            "SELECT DISTINCT user.name " +
                "FROM user, json_each(user.phone) " +
                "WHERE json_each.value LIKE '704-%'"
        ).usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals("Mike", SQLite.columnText(it, 0))
            assertEquals(SQL_DONE, SQLite.step(it))
        }
    }

    @Test
    fun jsonEachMixed() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE user (name TEXT, phone TEXT)"))
        SQLite.exec(db, "INSERT INTO user VALUES ('Mike', '[\"704-123\", \"704-456\"]')")
        SQLite.exec(db, "INSERT INTO user VALUES ('Bob', '704-123')")
        prepareStatement(
            db,
            "SELECT name FROM user WHERE phone LIKE '704-%' " +
                "UNION " +
                "SELECT user.name " +
                "FROM user, json_each(user.phone) " +
                "WHERE json_valid(user.phone) " +
                "AND json_each.value LIKE '704-%'"
        ).usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals("Bob", SQLite.columnText(it, 0))
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals("Mike", SQLite.columnText(it, 0))
            assertEquals(SQL_DONE, SQLite.step(it))
        }
    }

    @Test
    fun jsonTreeLeavesOnly() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE big (json TEXT)"))
        prepareStatement(db, "INSERT INTO big VALUES (?)").usePreparedStatement {
            JSONObject().put("msg", JSONObject().apply {
                put("subject", "xmas")
                put("body", "I want a Spiderman helmet for Christmas!")
            }).run {
                SQLite.bindText(it, 1, toString())
                assertEquals(SQL_DONE, SQLite.step(it))
            }
        }
        prepareStatement(
            db,
            "SELECT fullkey, path, key, value " +
                "FROM big, json_tree(big.json) " +
                "WHERE json_tree.type NOT IN ('object','array')"
        ).usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals("\$.msg.subject", SQLite.columnText(it, 0))
            assertEquals("\$.msg", SQLite.columnText(it, 1))
            assertEquals("subject", SQLite.columnText(it, 2))
            assertEquals("xmas", SQLite.columnText(it, 3))
        }
    }

    @Test
    fun jsonTreeLeavesOnlyBetter() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE big (json TEXT)"))
        prepareStatement(db, "INSERT INTO big VALUES (?)").usePreparedStatement {
            JSONObject().put("msg", JSONObject().apply {
                put("subject", "xmas")
                put("body", "I want a Spiderman helmet for Christmas!")
            }).run {
                SQLite.bindText(it, 1, toString())
                assertEquals(SQL_DONE, SQLite.step(it))
            }
        }
        prepareStatement(
            db,
            "SELECT fullkey, path, key, value " +
                "FROM big, json_tree(big.json) " +
                "WHERE atom IS NOT NULL"
        ).usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals("\$.msg.subject", SQLite.columnText(it, 0))
            assertEquals("\$.msg", SQLite.columnText(it, 1))
            assertEquals("subject", SQLite.columnText(it, 2))
            assertEquals("xmas", SQLite.columnText(it, 3))
        }
    }

    @Test
    fun jsonTreeLeavesPathKeyLikeQuery() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE big (json TEXT)"))
        prepareStatement(db, "INSERT INTO big VALUES (?)").usePreparedStatement {
            JSONObject().put("msg", JSONObject().apply {
                put("subject", "xmas")
                put("body", "I want a Spiderman helmet for Christmas!")
            }).run {
                SQLite.bindText(it, 1, toString())
                assertEquals(SQL_DONE, SQLite.step(it))
            }
        }
        prepareStatement(
            db,
            "SELECT value " +
                "FROM big, json_tree(big.json) " +
                "WHERE atom IS NOT NULL AND " +
                "key='subject' AND " +
                "path LIKE '%.msg'"
        ).usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals("xmas", SQLite.columnText(it, 0))
        }
    }

    @Test
    fun jsonEachWithArray() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE big (event TEXT, items TEXT)"))
        prepareStatement(db, "INSERT INTO big VALUES (?, ?)").usePreparedStatement {
            JSONArray().put("batman").put("SpiderMan").run {
                SQLite.bindText(it, 1, "xmas")
                SQLite.bindText(it, 2, toString())
                assertEquals(SQL_DONE, SQLite.step(it))
            }
        }
        prepareStatement(
            db,
            "SELECT big.event " +
                "FROM big, json_each(big.items) " +
                "WHERE json_each.value='SpiderMan'"
        ).usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals("xmas", SQLite.columnText(it, 0))
        }
    }

    @Test
    fun jsonTreeNested() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE big (json TEXT)"))
        prepareStatement(db, "INSERT INTO big VALUES (?)").usePreparedStatement {
            JSONObject().put("msg", JSONObject().apply {
                put("id", 123)
                put("subject", "xmas")
                put("body", "I want a Spiderman helmet for Christmas!")
            }).run {
                SQLite.bindText(it, 1, toString())
                assertEquals(SQL_DONE, SQLite.step(it))
            }
        }
        prepareStatement(
            db,
            "SELECT DISTINCT json_extract(big.json, '\$.msg.id') " +
                "FROM big, json_tree(big.json, '\$.msg') " +
                "WHERE json_tree.key='subject' " +
                "AND json_tree.value='xmas'"
        ).usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals(123, SQLite.columnInt(it, 0))
        }
    }

    private fun openConnection(flags: SQLOpenOperation = SQL_OPEN_READWRITE or SQL_OPEN_CREATE): Pointer {
        val holder = LongArray(1)
        assertEquals(SQL_OK, SQLite.openV2(file.absolutePath, flags, holder))
        return holder.first().also { assertNotEquals(NULL, it) }
    }
}
