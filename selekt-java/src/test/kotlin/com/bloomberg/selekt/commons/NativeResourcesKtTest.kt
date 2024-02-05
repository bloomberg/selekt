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

package com.bloomberg.selekt.commons

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class NativeResourcesKtTest {
    @Test
    fun commonOsNames() {
        assertTrue(osNames().intersect(listOf("darwin", "linux", "mac", "osx", "windows")).isNotEmpty())
    }

    @Test
    fun osNameDarwin() {
        assertEquals(listOf("darwin", "mac", "macos", "osx"), osNames("Mac OS"))
    }

    @Test
    fun osNameLinux() {
        assertEquals(listOf("linux"), osNames("Linux"))
    }

    @Test
    fun osNameWindows() {
        assertEquals(listOf("windows"), osNames("Windows 95"))
    }

    @Test
    fun commonPlatformIdentifier() {
        assertTrue(platformIdentifiers().isNotEmpty())
    }

    @Test
    fun commonLibraryExtension() {
        assertTrue(libraryExtensions().intersect(listOf(".dll", ".dylib", ".so")).isNotEmpty())
    }

    @Test
    fun commonLibraryResourceName() {
        libraryResourceNames("jni", "selekt").forEach {
            assertTrue(it.startsWith("jni"))
        }
    }

    @Test
    fun loadNonExistentEmbeddedLibrary() {
        assertFailsWith<IllegalStateException> { loadEmbeddedLibrary(javaClass.classLoader, "jni", "foo") }
    }
}
