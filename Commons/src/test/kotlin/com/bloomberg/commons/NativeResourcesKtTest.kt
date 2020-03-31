/*
 * Copyright 2020 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.commons

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NativeResourcesKtTest {
    @Test
    fun commonOsName() {
        assertTrue(osName() in arrayOf("darwin", "linux", "windows"))
    }

    @Test
    fun osNameDarwin() {
        assertEquals("darwin", osName("Mac OS"))
    }

    @Test
    fun osNameLinux() {
        assertEquals("linux", osName("Linux"))
    }

    @Test
    fun osNameWindows() {
        assertEquals("windows", osName("Windows 95"))
    }

    @Test
    fun commonPlatformIdentifier() {
        assertTrue(platformIdentifier().isNotBlank())
    }

    @Test
    fun commonLibraryExtension() {
        assertTrue(libraryExtension() in arrayOf(".dll", ".dylib", ".so"))
    }

    @Test
    fun commonLibraryResourceName() {
        assertTrue(libraryResourceName("jni", "selekt").startsWith("jni"))
    }

    @Test
    fun loadNonExistentEmbeddedLibrary() {
        assertThrows<IllegalStateException> { loadEmbeddedLibrary(javaClass.classLoader, "jni", "foo") }
    }
}
