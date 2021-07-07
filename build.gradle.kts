/*
 * Copyright 2021 Bloomberg Finance L.P.
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

import io.gitlab.arturbosch.detekt.Detekt
import java.util.Locale
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.ANDROID_GRADLE_PLUGIN}")
        classpath(kotlin("gradle-plugin", Versions.KOTLIN.version))
    }
}

repositories {
    mavenCentral()
}

plugins {
    jacoco
    id("io.gitlab.arturbosch.detekt") version Versions.DETEKT.version
    id("io.github.gradle-nexus.publish-plugin") version Versions.NEXUS_PLUGIN.version
    id("org.jetbrains.dokka") version Versions.DOKKA.version
    id("org.jlleitschuh.gradle.ktlint") version Versions.KTLINT_GRADLE_PLUGIN.version
}

group = selektGroupId
version = selektVersionName
logger.quiet("Group: $group; Version: $version")

nexusPublishing {
    repositories {
        sonatype()
    }
}

jacoco {
    toolVersion = Versions.JACOCO.version
}

subprojects {
    apply {
        plugin("selekt")
    }
}

subprojects {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("com.bloomberg:selekt-android")).apply {
                with(project(":AndroidLib"))
                because("we work with an unreleased version")
            }
            substitute(module("com.bloomberg:selekt-android-sqlcipher")).apply {
                using(project(":AndroidSQLCipher"))
                because("we work with an unreleased version")
            }
            substitute(module("com.bloomberg:selekt-annotations")).apply {
                with(project(":Annotations"))
                because("we work with an unreleased version")
            }
            substitute(module("com.bloomberg:selekt-api")).apply {
                with(project(":ApiLib"))
                because("we work with an unreleased version")
            }
            substitute(module("com.bloomberg:selekt-java")).apply {
                with(project(":Lib"))
                because("we work with an unreleased version")
            }
            substitute(module("com.bloomberg:selekt-sqlite3")).apply {
                with(project(":SQLite3"))
                because("we work with an unreleased version")
            }
        }
    }
}

dependencies {
    ktlint("com.pinterest:ktlint:${Versions.KTLINT}")
}

subprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            allWarningsAsErrors = true
            freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
            jvmTarget = "1.8"
        }
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        toolVersion = Versions.DETEKT.version
        input = files("src")
        config = files("${rootProject.projectDir}/config/detekt/config.yml")
        buildUponDefaultConfig = true
        parallel = false
        debug = false
        ignoreFailures = false

        reports.html.destination = file("$rootDir/build/reports/detekt/${project.name}-detekt.html")
    }
    tasks.withType<Detekt> {
        exclude("**/res/**")
        exclude("**/tmp/**")
    }

    pluginManager.withPlugin("jacoco") {
        configure<JacocoPluginExtension> {
            toolVersion = Versions.JACOCO.version
        }
    }

    pluginManager.withPlugin("signing") {
        configure<SigningExtension> {
            val signingKeyId: String? by project
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            project.afterEvaluate {
                configure<PublishingExtension> {
                    publications.forEach { sign(it) }
                }
            }
        }
    }

    pluginManager.withPlugin("org.jetbrains.dokka") {
        tasks.withType<DokkaTask>().configureEach {
            dokkaSourceSets.named("main") {
                sourceLink {
                    remoteUrl.set(java.net.URL("https://github.com/bloomberg/selekt/tree/master/" +
                        "${this@configureEach.project.name}/src/main/kotlin"))
                    localDirectory.set(file("src/main/kotlin"))
                }
                includeNonPublic.set(false)
                jdkVersion.set(JavaVersion.VERSION_1_8.majorVersion.toInt())
                noAndroidSdkLink.set(false)
                noJdkLink.set(false)
                noStdlibLink.set(false)
                outputDirectory.set(File("$rootDir/docs/kdoc"))
            }
        }
    }
}

allprojects {
    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
    }
    configure<KtlintExtension> {
        version.set(Versions.KTLINT.version)
        disabledRules.set(kotlin.collections.setOf("import-ordering", "indent"))
        reporters {
            reporter(ReporterType.HTML)
        }
    }
    tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask>().configureEach {
        reportsOutputDirectory.set(rootProject.layout.buildDirectory.dir("reports/ktlint/${project.name}/$name"))
    }
}

fun JacocoReportBase.initialise() {
    group = "verification"
    val block: (JacocoReport) -> Unit = {
        this@initialise.classDirectories.from(it.classDirectories)
        this@initialise.executionData.from(it.executionData)
        this@initialise.sourceDirectories.from(it.sourceDirectories)
    }
    subprojects {
        pluginManager.withPlugin("bb-jacoco-android") {
            pluginManager.withPlugin("com.android.library") {
                val capitalisedVariant = this@subprojects.extensions.getByType(
                    JacocoAndroidUnitTestReportExtension::class.java).preferredVariant.capitalize(Locale.ROOT)
                tasks.withType<JacocoReport> {
                    if (name.contains(capitalisedVariant)) {
                        block(this@withType)
                        this@initialise.dependsOn(this@withType)
                    }
                }
            }
        }
        pluginManager.withPlugin("jacoco") {
            pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                tasks.withType<JacocoReport> {
                    block(this@withType)
                    this@initialise.dependsOn(this@withType)
                }
            }
        }
    }
}

tasks.register<JacocoReport>("jacocoSelektTestReport") {
    initialise()
    description = "Generates a global JaCoCo coverage report."
    reports {
        csv.required.set(false)
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.register<JacocoCoverageVerification>("jacocoSelektCoverageVerification") {
    initialise()
    description = "Verifies JaCoCo coverage bounds globally."
    violationRules {
        rule {
            isEnabled = true
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.9761".toBigDecimal() // Does not include inlined blocks. Jacoco can't yet cover these.
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.9326".toBigDecimal() // Does not include inlined blocks. Jacoco can't yet cover these.
            }
        }
    }
    mustRunAfter("jacocoSelektTestReport")
}
