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

description = "Micrometer metrics integration for Selekt JDBC."

plugins {
    id("com.bloomberg.selekt.sbom")
    kotlin("jvm")
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
    alias(libs.plugins.detekt)
}

repositories {
    mavenCentral()
    google()
}

disableKotlinCompilerAssertions()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}

sourceSets {
    test {
        resources.srcDir(project(":selekt-jdbc").layout.buildDirectory.dir("intermediates/libs"))
    }
}

dependencies {
    api(libs.micrometer.core)
    compileOnly(projects.selektJdbc)
    testImplementation(projects.selektJdbc)
    testRuntimeOnly(projects.selektSqlite3Sqlcipher)
}

tasks.named("processTestResources") {
    dependsOn(":selekt-jdbc:buildHostSQLite")
}

publishing {
    publications.register<MavenPublication>("main") {
        from(components.getByName("java"))
        pom {
            commonInitialisation(project)
        }
    }
}
