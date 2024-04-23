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

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.util.Locale
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.jvm.Throws

private const val LIBRARY_PATH_KEY = "com.bloomberg.selekt.library_path"

@Suppress("Detekt.StringLiteralDuplication")
@JvmSynthetic
internal fun osNames(systemOsName: String = System.getProperty("os.name")) = systemOsName.lowercase(Locale.US).run {
    when {
        startsWith("mac") -> listOf("darwin", "mac", "macos", "osx")
        startsWith("windows") -> listOf("windows")
        else -> listOf(replace("\\s+", "_"))
    }
}

@JvmSynthetic
internal fun platformIdentifiers() = osNames().flatMap {
    val osArch = System.getProperty("os.arch")
    listOf('-', File.separatorChar).map { s -> "$it$s$osArch" } + it
}

@JvmSynthetic
internal fun libraryExtensions() = osNames().map {
    when (it) {
        "darwin", "mac", "osx" -> ".dylib"
        "windows" -> ".dll"
        else -> ".so"
    }
}.toSet()

@JvmSynthetic
internal fun libraryNames(
    parentDirectory: String,
    name: String
) = (platformIdentifiers() * libraryExtensions()).map {
    "$parentDirectory${File.separatorChar}${it.first}${File.separatorChar}lib$name${it.second}"
}

@Throws(IOException::class)
fun loadEmbeddedLibrary(loader: ClassLoader, parentDirectory: String, name: String) {
    val url = checkNotNull(libraryNames(parentDirectory, name).firstNotNullOfOrNull {
        loader.getResource(it)
    }) { "Failed to find resource with name: $name in directory: $parentDirectory" }

    @Suppress("NewApi") // Not used by Android.
    val file = createTempFile("lib$name", "lib").toFile()
    try {
        url.openStream().use { inputStream ->
            FileOutputStream(file).use {
                inputStream.copyTo(it)
            }
        }
        @Suppress("UnsafeDynamicallyLoadedCode")
        System.load(file.absolutePath)
    } finally {
        file.delete()
    }
}

@Suppress("NewApi")
private fun loadLibrary(
    libraryPath: String,
    parentDirectory: String,
    name: String
) {
    val path = libraryNames(parentDirectory, name).map {
        Path(libraryPath, it)
    }.first {
        Files.exists(it)
    }.toAbsolutePath()
    @Suppress("UnsafeDynamicallyLoadedCode")
    System.load(path.toString())
}

@Throws(IOException::class)
fun loadLibrary(
    loader: ClassLoader,
    parentDirectory: String,
    name: String
) {
    when (val libraryPath = System.getProperty(LIBRARY_PATH_KEY)) {
        null -> loadEmbeddedLibrary(loader, parentDirectory, name)
        else -> loadLibrary(libraryPath, parentDirectory, name)
    }
}
