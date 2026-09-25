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

package com.bloomberg.selekt.jdbc

import java.sql.ResultSet
import java.sql.Wrapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class JdbcExtensionsTest {
    @Test
    fun unwrapUsesReifiedClass() {
        val wrapper = mock<Wrapper>()
        whenever(wrapper.unwrap(String::class.java)).thenReturn("wrapped")

        assertEquals("wrapped", wrapper.unwrap<String>())
        verify(wrapper).unwrap(String::class.java)
    }

    @Test
    fun isWrapperForUsesReifiedClass() {
        val wrapper = mock<Wrapper>()
        whenever(wrapper.isWrapperFor(String::class.java)).thenReturn(true)

        assertTrue(wrapper.isWrapperFor<String>())
        verify(wrapper).isWrapperFor(String::class.java)
    }

    @Test
    fun getObjectByIndexUsesReifiedClass() {
        val resultSet = mock<ResultSet>()
        whenever(resultSet.getObject(1, String::class.java)).thenReturn("indexed")

        assertEquals("indexed", resultSet.getObject<String>(1))
        verify(resultSet).getObject(1, String::class.java)
    }

    @Test
    fun getObjectByLabelUsesReifiedClass() {
        val resultSet = mock<ResultSet>()
        whenever(resultSet.getObject("value", String::class.java)).thenReturn("labelled")

        assertEquals("labelled", resultSet.getObject<String>("value"))
        verify(resultSet).getObject("value", String::class.java)
    }
}
