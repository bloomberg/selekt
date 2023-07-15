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

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import java.net.URL
import java.time.Duration
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.gradle.ext.copyright
import org.jetbrains.gradle.ext.settings
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask

plugins {
    base
    id("io.gitlab.arturbosch.detekt") version Versions.DETEKT.version
    id("io.github.gradle-nexus.publish-plugin") version Versions.NEXUS_PLUGIN.version
    id("org.jetbrains.dokka") version Versions.DOKKA.version
    id("org.jetbrains.kotlinx.kover") version Versions.KOTLINX_KOVER.version
    id("org.jetbrains.qodana") version Versions.QODANA_PLUGIN.version
    id("org.jlleitschuh.gradle.ktlint") version Versions.KTLINT_GRADLE_PLUGIN.version
    id("org.jetbrains.gradle.plugin.idea-ext") version Versions.IDE_EXT_GRADLE_PLUGIN.version
}

repositories {
    mavenCentral()
}

group = selektGroupId
version = selektVersionName
logger.quiet("Group: {}; Version: {}", group, version)

nexusPublishing {
    repositories {
        sonatype()
    }
    transitionCheckOptions {
        maxRetries.set(180)
        delayBetween.set(Duration.ofSeconds(10L))
    }
}

dependencies {
    kover(projects.selektAndroid)
    kover(projects.selektApi)
    kover(projects.selektJava)
    ktlint("com.pinterest:ktlint:${Versions.KTLINT}")
}

subprojects {
    group = rootProject.group
    version = rootProject.version
    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }
    plugins.withType<JavaPlugin>().configureEach {
        tasks.withType<Jar>().configureEach {
            metaInf {
                from("$rootDir/LICENSE")
            }
        }
    }
    listOf("java", "com.android.library").forEach {
        plugins.withId(it) {
            dependencies {
                configurations.getByName("compileOnly").apply {
                    add(name, "com.google.code.findbugs:jsr305:[2.0.2, ${Versions.JSR_305}]")
                }
                configurations.getByName("implementation").apply {
                    platform(kotlinX("coroutines-bom", version = Versions.KOTLINX_COROUTINES.version))
                }
                configurations.getByName("testImplementation") {
                    add(name, kotlin("test", Versions.KOTLIN_TEST.version))
                    add(name, kotlinX("coroutines-core", version = Versions.KOTLINX_COROUTINES.version))
                    add(name, "org.mockito:mockito-core:${Versions.MOCKITO}")
                    add(name, "org.mockito.kotlin:mockito-kotlin:${Versions.MOCKITO_KOTLIN}")
                }
            }
        }
    }
    plugins.withId("com.android.application") {
        configure<ApplicationExtension> {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            lint {
                warningsAsErrors = true
            }
            testOptions {
                unitTests.isIncludeAndroidResources = true
            }
        }
    }
    plugins.withId("com.android.library") {
        configure<LibraryExtension> {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            lint {
                warningsAsErrors = true
            }
            testOptions {
                unitTests.isIncludeAndroidResources = true
            }
        }
    }
    tasks.withType<Test>().configureEach {
        systemProperty("com.bloomberg.selekt.lib.can_use_embedded", true)
    }
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            allWarningsAsErrors = true
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
            jvmTarget = "17"
        }
    }
    tasks.withType<Test>().configureEach {
        testLogging {
            events(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            exceptionFormat = TestExceptionFormat.FULL
            showCauses = true
            showExceptions = true
            showStackTraces = true
        }
        useJUnitPlatform()
        mapOf(
            "junit.jupiter.execution.parallel.enabled" to true,
            "junit.jupiter.execution.parallel.mode.default" to "concurrent",
            "junit.jupiter.execution.timeout.lifecycle.method.default" to "60s",
            "junit.jupiter.execution.timeout.mode" to "disabled_on_debug",
            "junit.jupiter.execution.timeout.testable.method.default" to "60s"
        ).forEach {
            systemProperty(it.key, it.value)
        }
    }
    configure<DetektExtension> {
        toolVersion = Versions.DETEKT.version
        source = files("src")
        config = files("${rootProject.projectDir}/config/detekt/config.yml")
        buildUponDefaultConfig = true
        parallel = false
        debug = false
        ignoreFailures = false
    }
    tasks.withType<Detekt>().configureEach {
        exclude("**/res/**")
        exclude("**/tmp/**")
        reports.html.outputLocation.fileValue(File("$rootDir/build/reports/detekt/${project.name}-detekt.html"))
    }
    plugins.withType<SigningPlugin> {
        configure<SigningExtension> {
            val signingKeyId: String? by project
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            configure<PublishingExtension> {
                isRequired = project.isRelease()
                publications {
                    sign(this)
                }
            }
        }
    }
    tasks.withType<DokkaTask>().configureEach {
        moduleName.set("Selekt")
        dokkaSourceSets.named("main") {
            sourceLink {
                remoteUrl.set(URL("https://github.com/bloomberg/selekt/tree/master/" +
                    "${this@configureEach.project.name}/src/main/kotlin"))
                localDirectory.set(file("src/main/kotlin"))
            }
            includeNonPublic.set(false)
            jdkVersion.set(JavaVersion.VERSION_17.majorVersion.toInt())
            noAndroidSdkLink.set(false)
            noJdkLink.set(false)
            noStdlibLink.set(false)
        }
    }
}

allprojects {
    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
    }
    configure<KtlintExtension> {
        version.set(Versions.KTLINT.version)
        disabledRules.set(setOf("import-ordering", "indent", "wrapping"))
        reporters {
            reporter(ReporterType.HTML)
        }
    }
    tasks.withType<GenerateReportsTask>().configureEach {
        reportsOutputDirectory.set(rootProject.layout.buildDirectory.dir("reports/ktlint/${project.name}/$name"))
    }
}

koverReport {
    defaults {
        filters {
            excludes {
                classes("*Test*")
                packages(listOf(
                    "*.benchamrks",
                    "*_generated"
                ))
            }
        }
        verify {
            rule("Minimal coverage") {
                bound {
                    minValue = 97
                    aggregation = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
}

tasks.getByName("check") {
    dependsOn("koverVerify")
}

idea.project.settings {
    copyright {
        useDefault = "Bloomberg"
        profiles {
            create("Bloomberg") {
                notice = """
Copyright ${'$'}today.year Bloomberg Finance L.P.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
""".trimIndent()
            }
        }
    }
}

qodana {
    saveReport.set(true)
}
