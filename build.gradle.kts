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
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.nexus)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.ideaExt)
    alias(libs.plugins.qodana)
    alias(libs.plugins.ksp) apply false
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
        maxRetries = 180
        delayBetween = Duration.ofSeconds(10L)
    }
}

dependencies {
    kover(projects.selektAndroid)
    kover(projects.selektApi)
    kover(projects.selektJava)
    kover(projects.selektSqlite3Classes)
}

subprojects {
    group = rootProject.group
    version = rootProject.version
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
                    add(name, "com.google.code.findbugs:jsr305:[2.0.2, ${libs.findbugs.jsr305.get().version}]")
                }
                configurations.getByName("implementation").apply {
                    add(name, platform(libs.kotlin.bom))
                    add(name, platform(libs.kotlinx.coroutines.bom))
                }
                configurations.getByName("testImplementation") {
                    add(name, libs.kotlin.test)
                    add(name, libs.kotlinx.coroutines.core)
                    add(name, libs.mockito.core)
                    add(name, libs.mockito.kotlin)
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
                disable.addAll(listOf(
                    "AndroidGradlePluginVersion",
                    "GradleDependency"
                ))
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
                disable.addAll(listOf(
                    "AndroidGradlePluginVersion",
                    "GradleDependency"
                ))
            }
            testOptions {
                unitTests.isIncludeAndroidResources = true
            }
        }
    }
    tasks.withType<Test>().configureEach {
        systemProperty("com.bloomberg.selekt.can_use_load", true)
        systemProperty(
            "com.bloomberg.selekt.library_path",
            layout.buildDirectory.dir("intermediates/assets/debugUnitTest/mergeDebugUnitTestAssets").get()
                .asFile.toString()
        )
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
    plugins.withId("io.gitlab.arturbosch.detekt") {
        configure<DetektExtension> {
            source = files("src")
            config = files("${rootProject.projectDir}/config/detekt/config.yml")
            buildUponDefaultConfig = true
            parallel = false
            debug = false
            ignoreFailures = false
        }
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
                remoteUrl = URL(
                    "https://github.com/bloomberg/selekt/tree/master/${this@configureEach.project.name}/src/main/kotlin"
                )
                localDirectory = file("src/main/kotlin")
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
    plugins.withId("org.jlleitschuh.gradle.ktlint") {
        configure<KtlintExtension> {
            disabledRules.set(setOf("import-ordering", "indent", "wrapping"))
            reporters {
                reporter(ReporterType.HTML)
            }
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
                    "*.benchmarks",
                    "*_generated"
                ))
            }
        }
        verify {
            rule("Minimal coverage") {
                bound {
                    minValue = 96
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
    saveReport = true
}
