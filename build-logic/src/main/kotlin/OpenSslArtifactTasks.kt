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

import java.io.File
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Removes unpacked OpenSSL source trees that do not match the configured version. */
abstract class RemoveStaleOpenSslSources @Inject constructor(
    private val fileSystemOperations: FileSystemOperations
) : DefaultTask() {
    @get:Internal
    abstract val generatedTargetDirectory: DirectoryProperty

    @get:Input
    abstract val expectedDirectoryName: Property<String>

    @TaskAction
    fun remove() {
        val expectedName = expectedDirectoryName.get()
        val staleDirectories = generatedTargetDirectory.get().asFile.listFiles()
            ?.filter { it.isDirectory && it.name.startsWith("openssl-") && it.name != expectedName }
            .orEmpty()
        staleDirectories.forEach {
            logger.lifecycle("Removing stale OpenSSL source directory: ${it.absolutePath}")
        }
        fileSystemOperations.delete {
            delete(staleDirectories)
        }
    }
}

@CacheableTask
abstract class VerifyOpenSslArtifacts : DefaultTask() {
    @get:Input
    abstract val expectedVersion: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val versionHeader: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val library: RegularFileProperty

    @get:OutputFile
    abstract val manifest: RegularFileProperty

    @TaskAction
    fun verifyAndRecord() {
        val expected = expectedVersion.get()
        val header = versionHeader.get().asFile
        val headerVersion = Regex("""(?m)^#\s*define\s+OPENSSL_VERSION_STR\s+"([^"]+)"\s*$""")
            .find(header.readText())
            ?.groupValues
            ?.get(1)
        check(headerVersion == expected) {
            "OpenSSL header version mismatch: expected $expected, found ${headerVersion ?: "none"}"
        }
        val libcrypto = library.get().asFile
        val output = manifest.get().asFile
        output.parentFile.mkdirs()
        output.writeText(
            "openssl.version=$expected\n" +
                "openssl.opensslv.sha256=${header.sha256()}\n" +
                "openssl.libcrypto.sha256=${libcrypto.sha256()}\n"
        )
    }

    private fun File.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val length = input.read(buffer)
                if (length < 0) {
                    break
                }
                digest.update(buffer, 0, length)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(Locale.ROOT, it) }
    }
}
