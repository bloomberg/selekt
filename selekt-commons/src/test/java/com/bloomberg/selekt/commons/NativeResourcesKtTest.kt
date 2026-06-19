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

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

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
    fun platformIdentifierAArch64IncludesArm64Alias() {
        val ids = platformIdentifiers(systemOsName = "Windows 11", systemOsArch = "aarch64")
        assertTrue("windows-aarch64" in ids)
        assertTrue("windows-arm64" in ids)
    }

    @Test
    fun platformIdentifierArm64IncludesAArch64Alias() {
        val ids = platformIdentifiers(systemOsName = "Windows 11", systemOsArch = "arm64")
        assertTrue("windows-aarch64" in ids)
        assertTrue("windows-arm64" in ids)
    }

    @Test
    fun platformIdentifierMacAArch64IncludesArm64Alias() {
        val ids = platformIdentifiers(systemOsName = "Mac OS X", systemOsArch = "aarch64")
        assertTrue("darwin-aarch64" in ids)
        assertTrue("darwin-arm64" in ids)
    }

    @Test
    fun platformIdentifierAmd64IncludesX8664Alias() {
        val ids = platformIdentifiers(systemOsName = "Linux", systemOsArch = "amd64")
        assertTrue("linux-amd64" in ids)
        assertTrue("linux-x86_64" in ids)
    }

    @Test
    fun archNamesCanonicaliseAArch64() {
        assertEquals(listOf("aarch64", "arm64"), archNames("aarch64"))
        assertEquals(listOf("aarch64", "arm64"), archNames("ARM64"))
    }

    @Test
    fun archNamesCanonicaliseAmd64() {
        assertEquals(listOf("amd64", "x86_64"), archNames("amd64"))
        assertEquals(listOf("amd64", "x86_64"), archNames("X86_64"))
    }

    @Test
    fun archNamesPassThroughUnknown() {
        assertEquals(listOf("riscv64"), archNames("riscv64"))
    }

    @Test
    fun commonLibraryExtension() {
        assertTrue(libraryExtensions().intersect(listOf(".dll", ".dylib", ".so")).isNotEmpty())
    }

    @Test
    fun commonLibraryResourceName() {
        libraryNames("jni", "selekt").forEach {
            assertTrue(it.startsWith("jni"))
        }
    }

    @Test
    fun loadNonExistentEmbeddedLibrary() {
        assertFailsWith<IllegalStateException> { loadEmbeddedLibrary(javaClass.classLoader, "jni", "foo") }
    }
}
