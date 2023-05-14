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

@file:Suppress("UnstableApiUsage")

import kotlinx.kover.api.KoverTaskExtension
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
    id("org.jetbrains.kotlinx.kover")
    id("bb-jmh")
}

disableKotlinCompilerAssertions()

java {
    withJavadocJar()
    withSourcesJar()
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        resources.srcDir("$buildDir/intermediates/libs")
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    implementation(projects.selektApi)
    implementation(projects.selektSqlite3)
    jmhImplementation(kotlinX("coroutines-core", version = Versions.KOTLINX_COROUTINES.version))
}

publishing {
    publications.register<MavenPublication>("main") {
        from(components.getByName("java"))
        pom {
            commonInitialisation(project)
            description.set("Selekt core library.")
        }
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    outputs.upToDateWhen { false }
    outputs.cacheIf { false }
    dependsOn("buildHostSQLite")
    shouldRunAfter("test")
    extensions.configure<KoverTaskExtension> {
        isDisabled.set(true)
    }
}

tasks.register<Task>("buildHostSQLite") {
    dependsOn(":selekt-sqlite3:buildHost")
    finalizedBy("copyJniLibs")
}

tasks.register<Copy>("copyJniLibs") {
    from(fileTree("${project(":selekt-sqlite3").buildDir.absolutePath}/intermediates/libs"))
    into("${buildDir.path}/intermediates/libs/jni")
}

tasks.withType<ProcessResources>().configureEach {
    dependsOn("buildHostSQLite", "copyJniLibs")
}

tasks.withType<DokkaTask>().configureEach {
    dependsOn("kaptKotlin") // FIXME Remove?
}

tasks.withType<DokkaTaskPartial>().configureEach {
    dependsOn("kaptKotlin") // FIXME Remove?
}
