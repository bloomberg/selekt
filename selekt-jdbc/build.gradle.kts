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

import me.champeau.jmh.JMHTask

description = "Selekt JDBC library."

plugins {
    kotlin("jvm")
    id("com.android.lint")
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
    alias(libs.plugins.jmh)
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

sourceSets {
    test {
        resources {
            srcDir(layout.buildDirectory.dir("intermediates/libs"))
        }
    }
    named("jmh") {
        resources.srcDir(layout.buildDirectory.dir("intermediates/libs"))
    }
}

dependencies {
    implementation(projects.selektApi)
    implementation(projects.selektCommons)
    implementation(projects.selektJava)
    implementation(projects.selektSqlite3Api)
    implementation(projects.selektSqlite3Classes) {
        capabilities {
            requireCapability("com.bloomberg.selekt:selekt-sqlite3-classes-java25")
        }
    }
    jmhImplementation(projects.selektSqlite3Classes) {
        capabilities {
            requireCapability("com.bloomberg.selekt:selekt-sqlite3-classes-java25")
        }
    }
    jmhImplementation(libs.xerial.sqlite.jdbc)
    implementation(libs.slf4j.api)
    testImplementation(platform(libs.exposed.bom))
    testImplementation(libs.exposed.core)
    testImplementation(libs.exposed.jdbc)
}

jmh {
    jvmArgs.add("-XX:+UseCompactObjectHeaders")
    resultFormat.set("JSON")
    if (hasProperty("jmh.includes")) {
        includes.add(property("jmh.includes").toString())
    }
    if (hasProperty("jmh.profilers")) {
        profilers.add(property("jmh.profilers").toString())
    }
    if (hasProperty("jmh.params")) {
        property("jmh.params").toString().split(";").forEach { entry ->
            val (key, value) = entry.split("=", limit = 2)
            benchmarkParameters.put(key, objects.listProperty(String::class.java).also { it.set(value.split(",")) })
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

tasks.named<JMHTask>("jmh") {
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
