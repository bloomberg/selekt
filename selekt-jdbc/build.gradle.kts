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
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    implementation(projects.selektSqlite3Classes)
    runtimeOnly(projects.selektSqlite3Sqlcipher)
    jmhImplementation(projects.selektSqlite3Classes)
    jmhImplementation(libs.xerial.sqlite.jdbc)
    implementation(libs.slf4j.api)
    testImplementation(platform(libs.exposed.bom))
    testImplementation(libs.exposed.core)
    testImplementation(libs.exposed.jdbc)
    testImplementation(libs.xerial.sqlite.jdbc)
}

val java25TestRuntimeClasspath = configurations.create("java25TestRuntimeClasspath") {
    isCanBeConsumed = false
    isCanBeResolved = true
    extendsFrom(configurations.testImplementation.get(), configurations.testRuntimeOnly.get())
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

jmh {
    resultFormat.set("JSON")
    if (hasProperty("jmh.includes")) {
        includes.add(property("jmh.includes").toString())
    }
    if (hasProperty("jmh.profilers")) {
        val jmhReportsDir = layout.buildDirectory.dir("reports/jmh").get().asFile.absolutePath
        property("jmh.profilers").toString().split(',').forEach {
            val profiler = it.trim()
            val resolved = when {
                profiler == "jfr" -> "jfr:dir=$jmhReportsDir"
                profiler.startsWith("jfr:") && !profiler.contains("dir=") -> "$profiler,dir=$jmhReportsDir"
                else -> profiler
            }
            profilers.add(resolved)
        }
    }
    if (hasProperty("jmh.params")) {
        property("jmh.params").toString().split(';').forEach { entry ->
            val (key, value) = entry.split('=', limit = 2)
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

tasks.withType<ProcessResources>().matching { it.name != "processResources" }.configureEach {
    dependsOn("buildHostSQLite")
}

tasks.named<JMHTask>("jmh") {
    dependsOn("buildHostSQLite")
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(11))
    })
}

tasks.register<Test>("testJava25") {
    description = "Runs the Java 11-compatible JDBC tests on Java 25 using the automatically selected FFM backend."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].output + sourceSets["main"].output + java25TestRuntimeClasspath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    dependsOn("testClasses")
    shouldRunAfter("test")
}

tasks.named("check") {
    dependsOn("testJava25")
}

publishing {
    publications.register<MavenPublication>("main") {
        from(components.getByName("java"))
        pom {
            commonInitialisation(project)
        }
    }
}
