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

@file:Suppress("UnstableApiUsage")

description = "Selekt JDBC library."

plugins {
    kotlin("jvm")
    `jvm-test-suite`
    id("com.android.lint")
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
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(projects.selektApi)
    implementation(projects.selektJava)
    implementation(projects.selektSqlite3Api)
    implementation(projects.selektSqlite3Classes) {
        capabilities {
            requireCapability("com.bloomberg.selekt:selekt-sqlite3-classes-java25")
        }
    }
    implementation(libs.slf4j.api)
}

testing {
    suites {
        val integrationTest by registering(JvmTestSuite::class) {
            sources {
                resources {
                    srcDir(layout.buildDirectory.dir("intermediates/libs"))
                }
            }
            dependencies {
                implementation(project())
                implementation(platform(libs.exposed.bom))
                implementation(platform(libs.kotlin.bom))
                implementation(projects.selektApi)
                implementation(projects.selektJava)
                implementation(projects.selektSqlite3Api)
                implementation(projects.selektSqlite3Classes) {
                    capabilities {
                        requireCapability("com.bloomberg.selekt:selekt-sqlite3-classes-java25")
                    }
                }
                implementation(libs.exposed.core)
                implementation(libs.exposed.jdbc)
                implementation(libs.kotlin.test)
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(tasks.test)
                        dependsOn("buildHostSQLite")
                    }
                }
            }
        }
    }
}

tasks.register<Task>("buildHostSQLite") {
    dependsOn(":SQLite3:buildHost", "copyJniLibs")
}

tasks.register<Copy>("copyJniLibs") {
    from(fileTree(project(":SQLite3").layout.buildDirectory.dir("intermediates/libs")))
    into(layout.buildDirectory.dir("intermediates/libs/jni"))
    mustRunAfter(":SQLite3:buildHost")
}

tasks.withType<ProcessResources>().configureEach {
    dependsOn("buildHostSQLite")
}

publishing {
    publications.register<MavenPublication>("main") {
        from(components.getByName("java"))
        pom {
            commonInitialisation(project)
        }
    }
}
