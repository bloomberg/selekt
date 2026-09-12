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

import java.util.zip.ZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class VerifyEmbeddedSbom : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val archives: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val sbom: RegularFileProperty

    @get:Input
    abstract val entryName: Property<String>

    @TaskAction
    fun verify() {
        val expected = sbom.get().asFile.readBytes()
        val expectedEntryName = entryName.get()

        require(!archives.isEmpty) {
            "No runtime JARs were provided for SBOM verification"
        }

        archives.forEach { archive ->
            ZipFile(archive).use { zip ->
                val entry = requireNotNull(zip.getEntry(expectedEntryName)) {
                    "${archive.name} does not contain $expectedEntryName"
                }
                val embedded = zip.getInputStream(entry).use { it.readBytes() }
                require(expected.contentEquals(embedded)) {
                    "$expectedEntryName in ${archive.name} differs from the published CycloneDX SBOM"
                }
            }
        }
    }
}
