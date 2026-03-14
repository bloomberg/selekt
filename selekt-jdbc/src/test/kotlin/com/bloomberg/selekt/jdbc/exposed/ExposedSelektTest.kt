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

package com.bloomberg.selekt.jdbc.exposed

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.sql.DriverManager
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val counter = AtomicInteger()

private object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val email = varchar("email", 255).nullable()
    override val primaryKey = PrimaryKey(id)
}

private object Posts : Table("posts") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val body = text("body")
    val authorId = integer("author_id").references(Users.id)
    override val primaryKey = PrimaryKey(id)
}

internal class ExposedSelektTest {
    @TempDir
    lateinit var tempDir: File

    private fun connect(): Database {
        val dbFile = File(tempDir, "test_${counter.incrementAndGet()}.db")
        val url = "jdbc:sqlite:${dbFile.absolutePath}"
        return Database.connect({ DriverManager.getConnection(url) })
    }

    @Test
    fun connectViaDriverManager() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            exec("SELECT 1") {
                assertTrue(it.next())
                assertEquals(1, it.getInt(1))
            }
        }
    }

    @Test
    fun createTable() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            assertTrue(SchemaUtils.listTables().any { it.equals("users", ignoreCase = true) })
        }
    }

    @Test
    fun createMultipleTables() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users, Posts)
            val tables = SchemaUtils.listTables().map { it.lowercase() }
            assertTrue(tables.contains("users"))
            assertTrue(tables.contains("posts"))
        }
    }

    @Test
    fun insertAndSelect() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            Users.insert {
                it[name] = "Alice"
                it[email] = "alice@example.com"
            }
            val rows = Users.selectAll().toList()
            assertEquals(1, rows.size)
            assertEquals("Alice", rows.first()[Users.name])
            assertEquals("alice@example.com", rows.first()[Users.email])
        }
    }

    @Test
    fun insertReturnsGeneratedId() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            val result = Users.insert {
                it[name] = "Bob"
                it[email] = null
            }
            val id = result[Users.id]
            assertNotNull(id)
            assertTrue(id > 0)
        }
    }

    @Test
    fun insertWithNullableColumn() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            Users.insert {
                it[name] = "Charlie"
                it[email] = null
            }
            val row = Users.selectAll().single()
            assertEquals("Charlie", row[Users.name])
            assertEquals(null, row[Users.email])
        }
    }

    @Test
    fun updateRow() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            Users.insert {
                it[name] = "Dave"
                it[email] = "dave@old.com"
            }
            val updated = Users.update({ Users.name eq "Dave" }) {
                it[email] = "dave@new.com"
            }
            assertEquals(1, updated)
            val row = Users.selectAll().single()
            assertEquals("dave@new.com", row[Users.email])
        }
    }

    @Test
    fun deleteRow() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            Users.insert {
                it[name] = "Eve"
                it[email] = null
            }
            Users.insert {
                it[name] = "Frank"
                it[email] = null
            }
            val deleted = Users.deleteWhere { Users.name eq "Eve" }
            assertEquals(1, deleted)
            val remaining = Users.selectAll().toList()
            assertEquals(1, remaining.size)
            assertEquals("Frank", remaining.first()[Users.name])
        }
    }

    @Test
    fun selectWithWhere() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            Users.insert { it[name] = "A"; it[email] = "a@x.com" }
            Users.insert { it[name] = "B"; it[email] = "b@x.com" }
            Users.insert { it[name] = "C"; it[email] = "c@x.com" }
            val results = Users.selectAll().where { Users.name eq "B" }.toList()
            assertEquals(1, results.size)
            assertEquals("B", results.first()[Users.name])
        }
    }

    @Test
    fun selectCount() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            repeat(5) { i ->
                Users.insert { it[name] = "User$i"; it[email] = null }
            }
            assertEquals(5L, Users.selectAll().count())
        }
    }

    @Test
    fun batchInsert() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            val names = listOf("Alpha", "Beta", "Gamma", "Delta")
            Users.batchInsert(names) { name ->
                this[Users.name] = name
                this[Users.email] = "$name@example.com"
            }
            assertEquals(4L, Users.selectAll().count())
        }
    }

    @Test
    fun foreignKeyRelationship() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users, Posts)
            val authorId = Users.insert {
                it[name] = "Author"
                it[email] = null
            }[Users.id]
            Posts.insert {
                it[title] = "First Post"
                it[body] = "Content"
                it[Posts.authorId] = authorId
            }
            val posts = Posts.selectAll().toList()
            assertEquals(1, posts.size)
            assertEquals("First Post", posts.first()[Posts.title])
            assertEquals(authorId, posts.first()[Posts.authorId])
        }
    }

    @Test
    fun multipleTransactions() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            Users.insert { it[name] = "First"; it[email] = null }
        }
        transaction(database) {
            Users.insert { it[name] = "Second"; it[email] = null }
        }
        transaction(database) {
            assertEquals(2L, Users.selectAll().count())
        }
    }

    @Test
    fun dropTable() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            assertTrue(SchemaUtils.listTables().any { it.equals("users", ignoreCase = true) })
            SchemaUtils.drop(Users)
            assertTrue(SchemaUtils.listTables().none { it.equals("users", ignoreCase = true) })
        }
    }

    @Test
    fun orderBySelect() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            Users.insert { it[name] = "Charlie"; it[email] = null }
            Users.insert { it[name] = "Alice"; it[email] = null }
            Users.insert { it[name] = "Bob"; it[email] = null }
            val sorted = Users.selectAll().orderBy(Users.name).map { it[Users.name] }
            assertEquals(listOf("Alice", "Bob", "Charlie"), sorted)
        }
    }

    @Test
    fun limitAndOffset() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            repeat(10) { i ->
                Users.insert { it[name] = "User${i.toString().padStart(2, '0')}"; it[email] = null }
            }
            val page = Users.selectAll().orderBy(Users.name).limit(3).offset(3).map { it[Users.name] }
            assertEquals(3, page.size)
            assertEquals("User03", page.first())
        }
    }

    @Test
    fun updateMultipleRows() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            Users.insert { it[name] = "A"; it[email] = null }
            Users.insert { it[name] = "B"; it[email] = null }
            Users.insert { it[name] = "C"; it[email] = null }
            val count = Users.update {
                it[email] = "updated@example.com"
            }
            assertEquals(3, count)
            Users.selectAll().forEach {
                assertEquals("updated@example.com", it[Users.email])
            }
        }
    }

    @Test
    fun deleteAll() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            Users.insert { it[name] = "A"; it[email] = null }
            Users.insert { it[name] = "B"; it[email] = null }
            Users.deleteWhere { Users.id greaterEq 1 }
            assertEquals(0L, Users.selectAll().count())
        }
    }

    @Test
    fun selectSpecificColumns() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            Users.insert { it[name] = "Alice"; it[email] = "alice@example.com" }
            val names = Users.select(Users.name).map { it[Users.name] }
            assertEquals(listOf("Alice"), names)
        }
    }

    @Test
    fun emptyTableSelect() {
        val database = connect()
        transaction(database) {
            SchemaUtils.create(Users)
            val rows = Users.selectAll().toList()
            assertTrue(rows.isEmpty())
            assertEquals(0L, Users.selectAll().count())
        }
    }
}
