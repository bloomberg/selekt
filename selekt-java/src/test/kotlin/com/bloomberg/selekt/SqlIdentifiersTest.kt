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

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class SqlIdentifiersTest {
    @Test
    fun quotesIdentifier() {
        assertEquals("\"odd\"\"name\"", StringBuilder().appendIdentifier("odd\"name").toString())
    }

    @Test
    fun quotesQualifiedIdentifier() {
        assertEquals(
            "\"main\".\"odd\"\"name\"",
            StringBuilder().appendQualifiedIdentifier("main.odd\"name").toString()
        )
    }

    @Test
    fun rejectsEmptyAndNulIdentifiers() {
        assertFailsWith<IllegalArgumentException> { StringBuilder().appendIdentifier("") }
        assertFailsWith<IllegalArgumentException> { StringBuilder().appendIdentifier("bad\u0000name") }
        assertFailsWith<IllegalArgumentException> { StringBuilder().appendQualifiedIdentifier("main..table") }
    }
}
