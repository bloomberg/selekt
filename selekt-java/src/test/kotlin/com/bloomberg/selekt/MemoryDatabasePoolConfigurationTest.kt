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

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

internal class MemoryDatabasePoolConfigurationTest {
    private val configuration = SQLiteJournalMode.WAL.databaseConfiguration.copy(maxConnectionPoolSize = 10)

    @Test
    fun `private memory database uses one connection`() {
        assertEquals(1, configuration.toPoolConfiguration(":memory:").maxTotal)
        assertEquals(1, configuration.toPoolConfiguration("file::memory:").maxTotal)
        assertEquals(1, configuration.toPoolConfiguration("file:private?mode=memory").maxTotal)
    }

    @Test
    fun `memory database retains its primary connection`() {
        assertEquals(true, ":memory:".isInMemoryDatabase())
        assertEquals(true, "file::memory:?cache=shared".isInMemoryDatabase())
        assertEquals(true, "file:shared?mode=memory&cache=shared".isInMemoryDatabase())
    }

    @Test
    fun `shared memory database retains configured pool size`() {
        assertEquals(10, configuration.toPoolConfiguration("file::memory:?cache=shared").maxTotal)
        assertEquals(10, configuration.toPoolConfiguration("file:shared?mode=memory&cache=shared").maxTotal)
    }

    @Test
    fun `file database retains configured pool size`() {
        assertEquals(10, configuration.toPoolConfiguration("database.sqlite").maxTotal)
        assertEquals(10, configuration.toPoolConfiguration("file:database.sqlite").maxTotal)
        assertEquals(false, "database.sqlite".isInMemoryDatabase())
    }
}
