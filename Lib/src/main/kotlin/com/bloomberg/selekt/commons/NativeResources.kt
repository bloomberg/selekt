/*
 * Copyright 2021 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.commons

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import kotlin.jvm.Throws

private const val DEFAULT_BUFFER_LIMIT = 8_192

@Suppress("Detekt.StringLiteralDuplication")
internal fun osName(systemOsName: String = System.getProperty("os.name")) = systemOsName.toLowerCase(Locale.US).run {
    when {
        startsWith("mac") -> "darwin"
        startsWith("windows") -> "windows"
        else -> replace("\\s+", "_")
    }
}

internal fun platformIdentifier() = "${osName()}-${System.getProperty("os.arch")}"

internal fun libraryExtension() = when (osName()) {
    "darwin" -> ".dylib"
    "windows" -> ".dll"
    else -> ".so"
}

internal fun libraryResourceName(parentDirectory: String, name: String) =
    "$parentDirectory/${platformIdentifier()}${File.separatorChar}lib${name}${libraryExtension()}"

@Throws(IOException::class)
fun loadEmbeddedLibrary(loader: ClassLoader, parentDirectory: String, name: String) {
    val url = libraryResourceName(parentDirectory, name).let {
        checkNotNull(loader.getResource(it)) { "Failed to find resource with name: $it" }
    }
    val file = File.createTempFile("libselekt", "lib").apply {
        delete()
        deleteOnExit()
    }
    url.openStream().use { inputStream ->
        BufferedOutputStream(FileOutputStream(file)).use { outputStream ->
            var length: Int
            val buffer = ByteArray(DEFAULT_BUFFER_LIMIT)
            while (inputStream.read(buffer).also { length = it } > -1) {
                outputStream.write(buffer, 0, length)
            }
        }
        loadNativeLibrary(file)
        file.delete()
    }
}

@Suppress("UnsafeDynamicallyLoadedCode")
private fun loadNativeLibrary(file: File) {
    file.run { require(isFile && exists() && canRead()) }
    System.load(file.absolutePath)
}
