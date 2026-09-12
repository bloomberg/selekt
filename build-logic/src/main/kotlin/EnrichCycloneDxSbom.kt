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

import java.util.Properties
import org.cyclonedx.Version
import org.cyclonedx.generators.json.BomJsonGenerator
import org.cyclonedx.model.Bom
import org.cyclonedx.model.Component
import org.cyclonedx.model.Dependency
import org.cyclonedx.model.ExternalReference
import org.cyclonedx.model.Hash
import org.cyclonedx.model.License
import org.cyclonedx.model.LicenseChoice
import org.cyclonedx.model.Property
import org.cyclonedx.parsers.JsonParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property as GradleProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Adds source-level native dependencies that Gradle cannot discover from JVM configurations. */
@CacheableTask
abstract class EnrichCycloneDxSbom : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val rawBom: RegularFileProperty

    @get:OutputFile
    abstract val enrichedBom: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val openSslProperties: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val sqliteVersionFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val sqliteHeader: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val vec1Source: RegularFileProperty

    @get:Input
    abstract val sqlCipherVersion: GradleProperty<String>

    @get:Input
    abstract val sqlCipherCommit: GradleProperty<String>

    @get:Input
    abstract val includeVec1: GradleProperty<Boolean>

    @get:Input
    abstract val mimallocVersion: GradleProperty<String>

    @get:Input
    abstract val mimallocSourceSha256: GradleProperty<String>

    @TaskAction
    fun enrich() {
        val parser = JsonParser()
        val bom = parser.parse(rawBom.get().asFile)
        val wrapper = findNativeWrapper(bom)
        wrapper.properties = (wrapper.properties.orEmpty() + listOf(
            Property(
                "selekt:native:published-platforms",
                "darwin-aarch64,linux-aarch64,linux-aarch64-musl,linux-amd64," +
                    "linux-amd64-musl,windows-aarch64,windows-amd64"
            ),
            Property("selekt:native:vec1-enabled", includeVec1.get().toString())
        )).distinctBy(Property::getName)

        val openSsl = Properties().apply {
            openSslProperties.get().asFile.inputStream().use(::load)
        }
        val openSslVersion = requireNotNull(openSsl.getProperty("openssl.version"))
        val openSslSha256 = requireNotNull(openSsl.getProperty("openssl.sha256"))
        val openSslPgpSha256 = requireNotNull(openSsl.getProperty("openssl.pgp.sha256"))
        val sqliteVersion = sqliteVersionFile.get().asFile.readText().trim()
        val sqliteSourceId = SQLITE_SOURCE_ID.find(sqliteHeader.get().asFile.readText())
            ?.groupValues?.get(1)
            ?: error("Could not find SQLITE_SOURCE_ID in ${sqliteHeader.get().asFile}")
        val vec1Version = VEC1_VERSION.find(vec1Source.get().asFile.readText())
            ?.groupValues?.get(1)
            ?: error("Could not find VEC1_VERSION in ${vec1Source.get().asFile}")

        val sqlCipher = nativeComponent {
            name = "SQLCipher"
            version = sqlCipherVersion.get()
            purl = "pkg:github/sqlcipher/sqlcipher@${sqlCipherVersion.get()}"
            licenseId = "BSD-3-Clause"
            description = "SQLite extension providing transparent 256-bit AES database encryption."
            sourceUrl = "https://github.com/sqlcipher/sqlcipher.git"
            sourceReferenceType = ExternalReference.Type.VCS
            properties = listOf(
                Property("selekt:native:git-commit", sqlCipherCommit.get())
            )
        }
        val sqlite = nativeComponent {
            name = "SQLite"
            version = sqliteVersion
            purl = "pkg:github/sqlite/sqlite@$sqliteVersion"
            licenseName = "SQLite Blessing"
            licenseUrl = "https://www.sqlite.org/copyright.html"
            description = "Embedded SQL database engine forming the base of SQLCipher."
            sourceUrl = "https://github.com/sqlite/sqlite.git"
            sourceReferenceType = ExternalReference.Type.VCS
            properties = listOf(
                Property("selekt:native:sqlite-source-id", sqliteSourceId)
            )
        }
        val openSslComponent = nativeComponent {
            name = "OpenSSL"
            version = openSslVersion
            purl = "pkg:github/openssl/openssl@$openSslVersion"
            licenseId = "Apache-2.0"
            description = "Cryptography and TLS toolkit used by SQLCipher."
            sourceUrl = "https://www.openssl.org/source/openssl-$openSslVersion.tar.gz"
            sourceSha256 = openSslSha256
            properties = listOf(
                Property("selekt:native:source-signature-sha256", openSslPgpSha256)
            )
        }
        val mimalloc = nativeComponent {
            name = "mimalloc"
            version = mimallocVersion.get()
            purl = "pkg:github/microsoft/mimalloc@${mimallocVersion.get()}"
            licenseId = "MIT"
            description = "General-purpose allocator statically linked into Selekt's Linux native variants."
            sourceUrl = "https://github.com/microsoft/mimalloc/archive/refs/tags/v${mimallocVersion.get()}.tar.gz"
            sourceSha256 = mimallocSourceSha256.get()
            scope = Component.Scope.OPTIONAL
            properties = listOf(
                Property(
                    "selekt:native:included-platforms",
                    "linux-aarch64,linux-aarch64-musl,linux-amd64,linux-amd64-musl"
                )
            )
        }

        val nativeComponents = mutableListOf(sqlCipher, sqlite, openSslComponent, mimalloc)
        val wrapperDependencies = mutableListOf(sqlCipher.bomRef, mimalloc.bomRef)
        if (includeVec1.get()) {
            val vec1 = nativeComponent {
                name = "vec1"
                version = vec1Version
                purl = "pkg:generic/sqlite-vec1@$vec1Version"
                licenseName = "SQLite Blessing"
                licenseUrl = "https://www.sqlite.org/copyright.html"
                description = "Optional SQLite vector-search extension compiled into Selekt native libraries."
                sourceUrl = "https://github.com/bloomberg/selekt.git"
                sourceReferenceType = ExternalReference.Type.VCS
                scope = Component.Scope.OPTIONAL
                properties = listOf(
                    Property("selekt:native:compiled-in", "true"),
                    Property("selekt:native:source-path", "SQLite3/sqlite3/extensions/vec1")
                )
            }
            nativeComponents += vec1
            wrapperDependencies += vec1.bomRef
        }

        nativeComponents.forEach { component ->
            if (bom.components.none { it.bomRef == component.bomRef }) {
                bom.addComponent(component)
            }
        }
        addDependencies(bom, wrapper.bomRef, wrapperDependencies)
        addDependencies(bom, sqlCipher.bomRef, listOf(sqlite.bomRef, openSslComponent.bomRef))
        listOf(sqlite, openSslComponent, mimalloc).forEach { component ->
            addDependencies(bom, component.bomRef, emptyList())
        }

        bom.components = bom.components.sortedBy(Component::getBomRef)
        bom.dependencies.forEach { dependency ->
            dependency.dependencies = dependency.dependencies.orEmpty()
                .distinctBy(Dependency::getRef)
                .sortedBy(Dependency::getRef)
        }
        bom.dependencies = bom.dependencies.distinctBy(Dependency::getRef).sortedBy(Dependency::getRef)

        val output = enrichedBom.get().asFile
        output.parentFile.mkdirs()
        val generated = BomJsonGenerator(bom, Version.VERSION_16).toJsonString(true) + System.lineSeparator()
        val validationErrors = parser.validate(generated, Version.VERSION_16)
        require(validationErrors.isEmpty()) {
            "Generated CycloneDX SBOM is invalid: ${validationErrors.joinToString()}"
        }
        output.writeText(generated)
    }

    private fun findNativeWrapper(bom: Bom): Component {
        val candidates = listOfNotNull(bom.metadata?.component) + bom.components
        return candidates.singleOrNull {
            it.group == "com.bloomberg.selekt" && it.name == "selekt-sqlite3-sqlcipher"
        } ?: error("SBOM does not contain com.bloomberg.selekt:selekt-sqlite3-sqlcipher")
    }

    private fun addDependencies(bom: Bom, parentRef: String, childRefs: List<String>) {
        val parent = bom.dependencies.firstOrNull { it.ref == parentRef }
            ?: Dependency(parentRef).also(bom::addDependency)
        val existing = parent.dependencies.orEmpty().mapTo(mutableSetOf(), Dependency::getRef)
        childRefs.filter(existing::add).forEach { parent.addDependency(Dependency(it)) }
    }

    private fun nativeComponent(configure: NativeComponentSpec.() -> Unit) = Component().apply {
        val spec = NativeComponentSpec().apply(configure)
        type = Component.Type.LIBRARY
        bomRef = spec.purl
        this.name = spec.name
        this.version = spec.version
        this.purl = spec.purl
        this.description = spec.description
        this.scope = spec.scope
        licenses = LicenseChoice().apply {
            addLicense(License().apply {
                id = spec.licenseId
                this.name = spec.licenseName
                url = spec.licenseUrl
            })
        }
        externalReferences = listOf(
            ExternalReference().apply {
                type = spec.sourceReferenceType
                url = spec.sourceUrl
                if (spec.sourceSha256 != null) {
                    hashes = listOf(Hash(Hash.Algorithm.SHA_256, spec.sourceSha256))
                }
            }
        )
        this.properties = spec.properties
    }

    private class NativeComponentSpec {
        lateinit var name: String
        lateinit var version: String
        lateinit var purl: String
        lateinit var description: String
        lateinit var sourceUrl: String
        var sourceReferenceType = ExternalReference.Type.SOURCE_DISTRIBUTION
        var licenseId: String? = null
        var licenseName: String? = null
        var licenseUrl: String? = null
        var sourceSha256: String? = null
        var scope = Component.Scope.REQUIRED
        var properties: List<Property> = emptyList()
    }

    private companion object {
        val SQLITE_SOURCE_ID = Regex("#define\\s+SQLITE_SOURCE_ID\\s+\\\"([^\\\"]+)\\\"")
        val VEC1_VERSION = Regex("#define\\s+VEC1_VERSION\\s+\\\"([^\\\"]+)\\\"")
    }
}
