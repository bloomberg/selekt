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

package com.bloomberg.selekt.jdbc.connection

import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.jdbc.driver.SharedDatabase
import com.bloomberg.selekt.jdbc.util.ConnectionURL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.SQLException
import java.sql.Savepoint
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class JdbcConnectionSavepointTest {
    private lateinit var mockDatabase: SQLDatabase
    private lateinit var connectionURL: ConnectionURL
    private lateinit var properties: Properties
    private lateinit var connection: JdbcConnection

    @BeforeEach
    fun setUp() {
        mockDatabase = mock()
        connectionURL = ConnectionURL.parse("jdbc:sqlite:/tmp/test.db")
        properties = Properties()
        connection = JdbcConnection(SharedDatabase(mockDatabase), connectionURL, properties)
    }

    @Test
    fun setSavepointInTransaction() {
        whenever(mockDatabase.setSavepoint(null)) doReturn "sp_user_12345"
        connection.run {
            autoCommit = false
            val savepoint = setSavepoint()
            assertNotNull(savepoint)
            assertEquals("sp_user_12345", savepoint.savepointName)
            assertEquals(0, savepoint.savepointId)
        }
        verify(mockDatabase).setSavepoint(null)
    }

    @Test
    fun setSavepointWithName() {
        whenever(mockDatabase.setSavepoint("my_savepoint")) doReturn "my_savepoint"
        connection.run {
            autoCommit = false
            val savepoint = setSavepoint("my_savepoint")
            assertNotNull(savepoint)
            assertEquals("my_savepoint", savepoint.savepointName)
        }
        verify(mockDatabase).setSavepoint("my_savepoint")
    }

    @Test
    fun setSavepointFailsInAutoCommit() {
        connection.run {
            assertTrue(autoCommit)
            assertFailsWith<SQLException> {
                setSavepoint()
            }
        }
    }

    @Test
    fun setSavepointWithNameFailsInAutoCommit() {
        connection.run {
            assertTrue(autoCommit)
            assertFailsWith<SQLException> {
                setSavepoint("test")
            }
        }
    }

    @Test
    fun rollbackToSavepoint() {
        whenever(mockDatabase.setSavepoint("test_sp")) doReturn "test_sp"
        connection.run {
            autoCommit = false
            val savepoint = setSavepoint("test_sp")
            rollback(savepoint)
        }
        verify(mockDatabase).setSavepoint("test_sp")
        verify(mockDatabase).rollbackToSavepoint("test_sp")
    }

    @Test
    fun rollbackToSavepointThrowsSQLException() {
        val savepoint = mock<Savepoint> {
            whenever(it.savepointName) doReturn "test_sp"
        }
        whenever(mockDatabase.rollbackToSavepoint("test_sp")) doThrow SQLException("Rollback failed")
        connection.run {
            autoCommit = false
            assertFailsWith<SQLException> {
                rollback(savepoint)
            }
        }
    }

    @Test
    fun releaseSavepoint() {
        whenever(mockDatabase.setSavepoint("test_sp")) doReturn "test_sp"
        connection.run {
            autoCommit = false
            val savepoint = setSavepoint("test_sp")
            releaseSavepoint(savepoint)
        }
        verify(mockDatabase).setSavepoint("test_sp")
        verify(mockDatabase).releaseSavepoint("test_sp")
    }

    @Test
    fun releaseSavepointThrowsSQLException() {
        val savepoint = mock<Savepoint> {
            whenever(it.savepointName) doReturn "test_sp"
        }
        whenever(mockDatabase.releaseSavepoint("test_sp")) doThrow SQLException("Release failed")
        connection.run {
            autoCommit = false
            assertFailsWith<SQLException> {
                releaseSavepoint(savepoint)
            }
        }
    }

    @Test
    fun generatedSavepointNamesAreUnique() {
        var callCount = 0
        whenever(mockDatabase.setSavepoint(null)).thenAnswer { "sp_user_${++callCount}" }
        connection.run {
            autoCommit = false
            val savepointOne = setSavepoint()
            val savepointTwo = setSavepoint()
            assertNotEquals(savepointOne.savepointName, savepointTwo.savepointName)
        }
    }

    @Test
    fun rollbackWithNullSavepoint() {
        connection.autoCommit = false
        connection.rollback(null)
    }

    @Test
    fun setSavepointWithDatabaseException() {
        whenever(mockDatabase.setSavepoint("test")) doThrow RuntimeException("Database error")
        connection.run {
            autoCommit = false
            assertFailsWith<SQLException> {
                setSavepoint("test")
            }
        }
    }

    @Test
    fun rollbackSavepointWithDatabaseException() {
        val savepoint = mock<Savepoint> {
            whenever(it.savepointName) doReturn "test_sp"
        }
        whenever(mockDatabase.rollbackToSavepoint("test_sp")) doThrow RuntimeException("Database error")
        connection.run {
            autoCommit = false
            assertFailsWith<SQLException> {
                rollback(savepoint)
            }
        }
    }

    @Test
    fun releaseSavepointWithDatabaseException() {
        val savepoint = mock<Savepoint> {
            whenever(it.savepointName) doReturn "test_sp"
        }
        whenever(mockDatabase.releaseSavepoint("test_sp")) doThrow RuntimeException("Database error")
        connection.run {
            autoCommit = false
            assertFailsWith<SQLException> {
                releaseSavepoint(savepoint)
            }
        }
    }
}
