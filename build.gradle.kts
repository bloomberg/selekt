/*
 * Copyright 2020 Bloomberg Finance L.P.
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

@file:Suppress("UnstableApiUsage")

import io.gitlab.arturbosch.detekt.Detekt
import java.io.ByteArrayOutputStream

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.ANDROID_GRADLE_PLUGIN}")
        classpath(kotlin("gradle-plugin", Versions.KOTLIN.version))
    }
}

apply {
    plugin("kotlin")
}

plugins {
    jacoco
    id("io.gitlab.arturbosch.detekt") version "1.1.1"
}

jacoco {
    toolVersion = Versions.JACOCO.version
}

allprojects {
    apply {
        plugin("selekt")
    }
}

subprojects {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("com.bloomberg.selekt:selekt-android")).apply {
                with(project(":AndroidLib"))
                @Suppress("UnstableApiUsage")
                because("we work with an unreleased version")
            }
            substitute(module("com.bloomberg.selekt:selekt-api")).apply {
                with(project(":ApiLib"))
                @Suppress("UnstableApiUsage")
                because("we work with an unreleased version")
            }
            substitute(module("com.bloomberg.selekt:selekt-commons")).apply {
                with(project(":Commons"))
                @Suppress("UnstableApiUsage")
                because("we work with an unreleased version")
            }
            substitute(module("com.bloomberg.selekt:selekt-java")).apply {
                with(project(":Lib"))
                @Suppress("UnstableApiUsage")
                because("we work with an unreleased version")
            }
            substitute(module("com.bloomberg.selekt:selekt-pools")).apply {
                with(project(":Pools"))
                @Suppress("UnstableApiUsage")
                because("we work with an unreleased version")
            }
            substitute(module("com.bloomberg.selekt:selekt-sqlite3")).apply {
                with(project(":SQLite3"))
                @Suppress("UnstableApiUsage")
                because("we work with an unreleased version")
            }
        }
    }
}

val ktlint: Configuration by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:${Versions.KTLINT}")
}

tasks.register<JavaExec>("ktlint") {
    group = "verification"
    description = "Check Kotlin code style."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args = listOf(
        "--reporter=plain",
        "--reporter=checkstyle,output=$buildDir/ktlint.xml",
        "--disabled_rules=import-ordering",
        "**/*.kt"
    )
}

subprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            allWarningsAsErrors = true
            freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
            jvmTarget = "1.8"
        }
    }

    detekt {
        toolVersion = "1.0.0-RC16"
        input = files("src")
        config = files("${rootProject.projectDir}/config/detekt/config.yml")
        parallel = true
        debug = false
        ignoreFailures = false

        reports.html.destination = file("$rootDir/build/reports/detekt/${project.name}-detekt.html")
    }
    tasks.withType<Detekt> {
        exclude("**/res/**")
        exclude("**/tmp/**")
    }
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.register("checkJavaVersion") {
    group = "verification"
    val javaHome = requireNotNull(System.getProperty("java.home"))
    val stderr = ByteArrayOutputStream()
    exec {
        commandLine("$javaHome/bin/java", "-version")
        errorOutput = stderr
    }
    val version = stderr.toString()
    logger.quiet(version)
    assert(Regex(".*OpenJDK.*build 1\\.8\\..*").containsMatchIn(version)) {
        "Gradle's Java home is currently: '$javaHome'. However AdoptOpenJDK 8 is required."
    }
}
