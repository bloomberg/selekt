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

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class SQLDatabaseSessionTest {
    @Test
    fun `active state delegates to database`() {
        val sqlSession = mock<SQLSession>()
        val database = mock<SQLDatabase> {
            whenever(it.isSessionActive(sqlSession)) doReturn false
        }
        val session = SQLDatabaseSession(database, sqlSession)

        assertFalse(session.isActiveOnCurrentThread)
        whenever(database.isSessionActive(sqlSession)) doReturn true
        assertTrue(session.isActiveOnCurrentThread)
    }
}
