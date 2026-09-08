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
    implementation(projects.selektJvm)
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
    testImplementation(libs.jazzer.junit)
    testImplementation(libs.xerial.sqlite.jdbc)
}

val jvmFuzzProfile = providers.gradleProperty("selekt.fuzz.profile").getOrElse("smoke")
val jvmFuzzDuration = when (jvmFuzzProfile) {
    "smoke" -> "5s"
    "release" -> "1m"
    "scheduled" -> "5m"
    else -> error("Unsupported JVM fuzzing profile: $jvmFuzzProfile")
}
val jvmFuzzTargets = mapOf(
    "jvmFuzzUtf8Reader" to "com.bloomberg.selekt.jdbc.result.Utf8ReaderFuzzTest.fuzzReader",
    "jvmFuzzConnectionUrl" to "com.bloomberg.selekt.jdbc.util.ConnectionURLFuzzTest.fuzzConnectionUrl",
    "jvmFuzzKeyEncoding" to "com.bloomberg.selekt.jdbc.driver.KeyEncodingFuzzTest.fuzzKeyEncoding",
    "jvmFuzzTypeMapping" to "com.bloomberg.selekt.jdbc.util.TypeMappingFuzzTest.fuzzTypeMapping",
    "jvmFuzzStateMachine" to "com.bloomberg.selekt.jdbc.driver.JdbcStateMachineFuzzTest.fuzzJdbcStateMachine"
)
val jvmFuzzTasks = jvmFuzzTargets.map { (taskName, testName) ->
    tasks.register<Test>(taskName) {
        description = "Runs the $testName Jazzer campaign."
        group = "verification"
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        environment("JAZZER_FUZZ", "1")
        filter {
            includeTestsMatching(testName)
        }
        systemProperty("jazzer.instrument", "com.bloomberg.selekt.jdbc.**")
        systemProperty("jazzer.max_duration", jvmFuzzDuration)
        systemProperty("jazzer.reproducer_path", layout.buildDirectory.get().asFile.absolutePath)
        systemProperty("junit.jupiter.execution.parallel.enabled", false)
        maxHeapSize = "1g"
        outputs.upToDateWhen { false }
        workingDir(layout.buildDirectory.get().asFile)
    }
}
jvmFuzzTasks.zipWithNext().forEach { (previous, next) ->
    next.configure {
        mustRunAfter(previous)
    }
}

tasks.register("jvmFuzz") {
    description = "Runs JVM fuzzing; configure with -Pselekt.fuzz.profile=smoke|release|scheduled."
    group = "verification"
    dependsOn(jvmFuzzTasks)
}

kover {
    currentProject {
        instrumentation {
            disabledForTestTasks.addAll(jvmFuzzTargets.keys)
        }
    }
}

jmh {
    jvmArgs.add("-XX:+UseCompactObjectHeaders")
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
}

publishing {
    publications.register<MavenPublication>("main") {
        from(components.getByName("java"))
        pom {
            commonInitialisation(project)
        }
    }
}
