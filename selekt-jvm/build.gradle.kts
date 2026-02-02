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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "Selekt SQLite JVM library."

plugins {
    kotlin("jvm")
    id("com.android.lint")
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.cash.licensee)
    `maven-publish`
    signing
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
    google()
}

disableKotlinCompilerAssertions()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
    kotlinJavaToolchain.toolchain.use(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    )
}

dependencies {
    api(projects.selektJava)
    implementation(projects.selektSqlite3Classes) {
        capabilities {
            requireCapability("com.bloomberg.selekt:selekt-sqlite3-classes-java25")
        }
    }
}

publishing {
    publications.register<MavenPublication>("main") {
        from(components.getByName("java"))
        pom {
            commonInitialisation(project)
        }
    }
}

licensee {
    allow("Apache-2.0")
}
