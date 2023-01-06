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

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
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
    extendsFrom(configurations.implementation.get())
}
val integrationTestRuntimeOnly: Configuration by configurations.getting { extendsFrom(configurations.runtimeOnly.get()) }

dependencies {
    implementation(selekt("api", selektVersionName))
    implementation(selekt("sqlite3", selektVersionName))
    integrationTestImplementation(selekt("api", selektVersionName))
    integrationTestImplementation(selekt("sqlite3", selektVersionName))
    integrationTestImplementation(kotlinX("coroutines-core"))
    integrationTestImplementation(kotlinX("coroutines-jdk8"))
    integrationTestImplementation(kotlin("test", Versions.KOTLIN_TEST.version))
    integrationTestImplementation(kotlin("test-junit", Versions.KOTLIN_TEST.version))
    integrationTestImplementation("org.junit.jupiter:junit-jupiter:${Versions.JUNIT5}")
    integrationTestRuntimeOnly("org.junit.platform:junit-platform-launcher:${Versions.JUNIT5_PLATFORM}")
    jmhImplementation(kotlinX("coroutines-core"))
    jmhImplementation(kotlinX("coroutines-jdk8"))
}

tasks.register("assembleSelekt") {
    dependsOn("assemble")
    dependsOn("sourcesJar")
}

publishing {
    publications.register<MavenPublication>("main") {
        groupId = selektGroupId
        artifactId = "selekt-java"
        version = selektVersionName
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
        isDisabled = true
    }
}

tasks.register<Task>("buildHostSQLite") {
    dependsOn(":SQLite3:buildHost")
    finalizedBy("copyJniLibs")
}

tasks.register<Copy>("copyJniLibs") {
    from(fileTree("${project(":SQLite3").buildDir.absolutePath}/intermediates/libs"))
    into("${buildDir.path}/intermediates/libs/jni")
}

tasks.withType<ProcessResources>().configureEach {
    mustRunAfter("buildHostSQLite")
}
