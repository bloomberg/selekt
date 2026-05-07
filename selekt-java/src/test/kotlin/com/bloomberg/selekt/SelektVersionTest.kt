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
import kotlin.test.assertTrue

internal class SelektVersionTest {
    @Test
    fun versionIsNotDefault() {
        assertTrue(SelektVersion.version != "0.0.0", "Version should be read from properties, not the default.")
    }

    @Test
    fun versionMatchesComponents(): Unit = SelektVersion.version.split('.').let {
        assertTrue(it.size >= 3, "Version must have at least 3 components: ${SelektVersion.version}")
        assertEquals(it[0].toInt(), SelektVersion.majorVersion)
        assertEquals(it[1].toInt(), SelektVersion.minorVersion)
        assertEquals(it[2].substringBefore('-').toInt(), SelektVersion.patchVersion)
    }

    @Test
    fun majorVersionIsNonNegative() {
        assertTrue(SelektVersion.majorVersion >= 0)
    }

    @Test
    fun minorVersionIsNonNegative() {
        assertTrue(SelektVersion.minorVersion >= 0)
    }

    @Test
    fun patchVersionIsNonNegative() {
        assertTrue(SelektVersion.patchVersion >= 0)
    }
}

