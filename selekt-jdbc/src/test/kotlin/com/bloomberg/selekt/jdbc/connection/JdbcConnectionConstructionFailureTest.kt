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
import java.sql.SQLException
import java.util.Properties
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class JdbcConnectionConstructionFailureTest {
    private val url = ConnectionURL.parse("jdbc:sqlite:/tmp/m01.db")

    @Test
    fun constructorFailureLeavesSharedDatabaseRetainCountUntouched() {
        val database: SQLDatabase = mock {
            whenever(it.exec(any<String>(), anyOrNull<Array<out Any?>>())) doThrow
                SQLException("simulated PRAGMA failure")
        }
        val releaseCount = AtomicInteger(0)
        val shared = SharedDatabase(database, onClose = { releaseCount.incrementAndGet() })
        shared.retain()
        assertFailsWith<SQLException> {
            JdbcConnection(shared, url, Properties())
        }
        assertTrue(shared.isOpen(), "SharedDatabase must remain open after constructor failure")
        assertEquals(0, releaseCount.get(), "JdbcConnection must not invoke onReleased")
        shared.release()
        assertTrue(shared.isOpen(), "Cache reference must still hold the SharedDatabase open")
        assertEquals(0, releaseCount.get())
        shared.release()
        assertFalse(shared.isOpen())
        assertEquals(1, releaseCount.get(), "onReleased must fire exactly once after final release")
    }

    @Test
    fun constructorSuccessDoesNotConsumeSharedDatabaseReference() {
        val database: SQLDatabase = mock {
            whenever(it.exec(any<String>(), anyOrNull<Array<out Any?>>())).then { }
        }
        val releaseCount = AtomicInteger(0)
        val shared = SharedDatabase(database, onClose = { releaseCount.incrementAndGet() })
        shared.retain()
        val connection = JdbcConnection(shared, url, Properties())
        assertTrue(shared.isOpen())
        assertEquals(0, releaseCount.get())
        connection.close()
        assertTrue(shared.isOpen(), "Cache reference must still hold the SharedDatabase open")
        assertEquals(0, releaseCount.get())
        shared.release()
        assertFalse(shared.isOpen())
        assertEquals(1, releaseCount.get())
    }
}
