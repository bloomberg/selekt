/*
 * Copyright 2022 Bloomberg Finance L.P.
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

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.util.Locale
import kotlin.jvm.Throws

@Suppress("Detekt.StringLiteralDuplication")
@JvmSynthetic
internal fun osName(systemOsName: String = System.getProperty("os.name")) = systemOsName.lowercase(Locale.US).run {
    when {
        startsWith("mac") -> "darwin"
        startsWith("windows") -> "windows"
        else -> replace("\\s+", "_")
    }
}

@JvmSynthetic
internal fun platformIdentifier() = "${osName()}-${System.getProperty("os.arch")}"

@JvmSynthetic
internal fun libraryExtension() = when (osName()) {
    "darwin" -> ".dylib"
    "windows" -> ".dll"
    else -> ".so"
}

@JvmSynthetic
internal fun libraryResourceName(parentDirectory: String, name: String) =
    "$parentDirectory/${platformIdentifier()}${File.separatorChar}lib${name}${libraryExtension()}"

@Suppress("NewApi") // Not used by Android.
@Throws(IOException::class)
fun loadEmbeddedLibrary(loader: ClassLoader, parentDirectory: String, name: String) {
    val url = libraryResourceName(parentDirectory, name).let {
        checkNotNull(loader.getResource(it)) { "Failed to find resource with name: $it" }
    }
    val file = Files.createTempFile("libselekt", "lib").toFile().apply {
        deleteOnExit()
    }
    try {
        url.openStream().use { inputStream ->
            BufferedOutputStream(FileOutputStream(file)).use { outputStream -> inputStream.copyTo(outputStream) }
        }
        loadNativeLibrary(file)
    } finally {
        file.delete()
    }
}

@Suppress("UnsafeDynamicallyLoadedCode")
private fun loadNativeLibrary(file: File) {
    file.run { require(isFile && exists() && canRead()) }
    System.load(file.absolutePath)
}
