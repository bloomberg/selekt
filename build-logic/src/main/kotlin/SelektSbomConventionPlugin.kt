import org.cyclonedx.model.ExternalReference
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.from
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

private const val CYCLONEDX_DIRECT_TASK = "cyclonedxDirectBom"
private const val FINAL_SBOM_TASK = "cyclonedxFinalBom"

class SelektSbomConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        val nativeAware = path in setOf(":selekt-jdbc", ":selekt-jvm", ":selekt-sqlite3-sqlcipher")
        pluginManager.apply("org.cyclonedx.bom")

        val directOutput = layout.buildDirectory.file(
            "reports/cyclonedx-direct/${project.name}-${project.version}-" +
                if (nativeAware) "raw.json" else "cyclonedx.json"
        )
        val directSbom = tasks.named(CYCLONEDX_DIRECT_TASK) {
            setCycloneDxProperty(this, "IncludeConfigs", listOf("runtimeClasspath"))
            setCycloneDxProperty(this, "TestConfigs", emptyList<String>())
            setCycloneDxProperty(this, "IncludeBomSerialNumber", false)
            setCycloneDxProperty(this, "IncludeBuildSystem", false)
            setCycloneDxProperty(this, "ExternalReferences", listOf(ExternalReference().apply {
                type = ExternalReference.Type.VCS
                url = "https://github.com/bloomberg/selekt.git"
            }))
            invokeCycloneDxMethod(this, "getXmlOutput", "unsetConvention")
            setCycloneDxProperty(this, "JsonOutput", directOutput)
        }

        val enrichmentTask = if (nativeAware) {
            tasks.register<EnrichCycloneDxSbom>("enrichCycloneDxSbom") {
                group = "reporting"
                description = "Adds bundled native dependencies to the CycloneDX SBOM."
                dependsOn(directSbom)
                rawBom.set(directOutput)
                enrichedBom.set(layout.buildDirectory.file(
                    "reports/cyclonedx-direct/${project.name}-${project.version}-cyclonedx.json"
                ))
                openSslProperties.set(layout.projectDirectory.file("../OpenSSL/openssl.properties"))
                sqliteVersionFile.set(
                    layout.projectDirectory.file("../SQLite3/src/main/external/sqlcipher/VERSION")
                )
                sqliteHeader.set(
                    layout.projectDirectory.file("../SQLite3/src/main/external/sqlcipher/sqlite3.h")
                )
                vec1Source.set(layout.projectDirectory.file("../SQLite3/sqlite3/extensions/vec1/vec1.c"))
                sqlCipherVersion.set(providers.gradleProperty("sqlcipher.version"))
                sqlCipherCommit.set(
                    providers.exec {
                        commandLine("git", "rev-parse", "HEAD:SQLite3/src/main/external/sqlcipher")
                    }.standardOutput.asText.map(String::trim)
                )
                includeVec1.set(
                    providers.gradleProperty("selekt.vec1.enabled")
                        .orElse(providers.environmentVariable("SELEKT_ENABLE_VEC1"))
                        .orElse("ON")
                        .map { it.equals("ON", ignoreCase = true) }
                )
                mimallocVersion.set("3.3.2")
                mimallocSourceSha256.set("ca02384e007f46950598500dfaebde5ff9948c1d231f5a81b058799afa64bbbb")
            }
        } else {
            null
        }
        val publishedSbom = enrichmentTask?.flatMap { it.enrichedBom } ?: directOutput
        tasks.register(FINAL_SBOM_TASK) {
            group = "reporting"
            dependsOn(directSbom)
            enrichmentTask?.let { dependsOn(it) }
        }

        plugins.withId("maven-publish") {
            extensions.configure<org.gradle.api.publish.PublishingExtension> {
                publications.withType<MavenPublication>().configureEach {
                    artifact(publishedSbom) {
                        classifier = "cyclonedx"
                        extension = "json"
                        builtBy(directSbom)
                        enrichmentTask?.let { builtBy(it) }
                    }
                }
            }
        }

        plugins.withId("java") {
            val sbomFileName = "${project.name}.cdx.json"
            val sbomJars = tasks.withType<Jar>().matching {
                it.name == "jar" || it.name == "uberJar" || it.name.matches(Regex("java\\d+Jar"))
            }
            val verifiedRuntimeJars = tasks.withType<Jar>().matching {
                it.name == "jar" || it.name.matches(Regex("java\\d+Jar"))
            }
            val verifyEmbeddedSbom = tasks.register<VerifyEmbeddedSbom>("verifyEmbeddedSbom") {
                group = "verification"
                description = "Verifies that runtime JARs embed their published CycloneDX SBOM."
                sbom.set(publishedSbom)
                entryName.set("META-INF/sbom/$sbomFileName")
            }
            sbomJars.all {
                dependsOn(directSbom)
                enrichmentTask?.let { dependsOn(it) }
                from(publishedSbom) {
                    into("META-INF/sbom")
                    rename { sbomFileName }
                }
            }
            verifyEmbeddedSbom.configure {
                dependsOn(verifiedRuntimeJars)
                archives.from(verifiedRuntimeJars)
            }
            if (path != ":selekt-sqlite3-sqlcipher") {
                tasks.named("check").configure { dependsOn(verifyEmbeddedSbom) }
            }
        }
    }
}

private fun setCycloneDxProperty(target: Any, property: String, value: Any) {
    val setter = target.javaClass.methods.firstOrNull { it.name == "set$property" && it.parameterCount == 1 }
        ?: error("Missing set$property on ${target.javaClass.name}")
    setter.invoke(target, value)
}

private fun invokeCycloneDxMethod(target: Any, getter: String, method: String, vararg args: Any) {
    val getterMethod = target.javaClass.methods.firstOrNull { it.name == getter && it.parameterCount == 0 }
        ?: error("Missing $getter on ${target.javaClass.name}: ${target.javaClass.methods.map { it.name }.distinct()}")
    val property = getterMethod.invoke(target)
    val setter = property.javaClass.methods.firstOrNull { it.name == method && it.parameterCount == args.size }
        ?: error("Missing $method on ${property.javaClass.name}: ${property.javaClass.methods.map { it.name }.distinct()}")
    setter.invoke(property, *args)
}
