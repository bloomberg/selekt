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
    id("com.bloomberg.selekt.sbom")
    kotlin("jvm")
    id("com.android.lint")
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
    alias(libs.plugins.jmh)
    alias(libs.plugins.detekt)
}

tasks.named("enrichCycloneDxSbom") {
    dependsOn(":SQLite3:amalgamateSQLite")
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
    testImplementation(libs.archunit)
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
    "jvmFuzzMetadataSqlIsolation" to
        "com.bloomberg.selekt.jdbc.metadata.JdbcMetadataSqlIsolationFuzzTest.fuzzMetadataSqlIsolation",
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
        systemProperty("junit.jupiter.execution.timeout.mode", "disabled")
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
            disabledForTestTasks.addAll(jvmFuzzTargets.keys + "jvmFuzzMetadataSqlIsolationJava25")
        }
    }
}

val jmhSqlite3ClassesJava11 = configurations.create("jmhSqlite3ClassesJava11") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 11)
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

val jmhSqlite3ClassesJava25 = configurations.create("jmhSqlite3ClassesJava25") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

dependencies {
    jmhSqlite3ClassesJava11(projects.selektSqlite3Classes)
    jmhSqlite3ClassesJava25(projects.selektSqlite3Classes) {
        capabilities {
            requireCapability("com.bloomberg.selekt:selekt-sqlite3-classes-java25")
        }
    }
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

val jvmFuzzMetadataSqlIsolationJava25 = tasks.register<Test>("jvmFuzzMetadataSqlIsolationJava25") {
    description = "Runs the metadata SQL-isolation Jazzer campaign on the Java 25 FFM backend."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().output + sourceSets.main.get().output + java25TestRuntimeClasspath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    environment("JAZZER_FUZZ", "1")
    filter {
        includeTestsMatching(
            "com.bloomberg.selekt.jdbc.metadata.JdbcMetadataSqlIsolationFuzzTest.fuzzMetadataSqlIsolation"
        )
    }
    systemProperty("jazzer.instrument", "com.bloomberg.selekt.jdbc.**")
    systemProperty("jazzer.max_duration", jvmFuzzDuration)
    systemProperty("jazzer.reproducer_path", layout.buildDirectory.get().asFile.absolutePath)
    systemProperty("junit.jupiter.execution.parallel.enabled", false)
    systemProperty("junit.jupiter.execution.timeout.mode", "disabled")
    maxHeapSize = "1g"
    outputs.upToDateWhen { false }
    workingDir(layout.buildDirectory.get().asFile)
    mustRunAfter(jvmFuzzTasks.last())
}

tasks.named("jvmFuzz") {
    dependsOn(jvmFuzzMetadataSqlIsolationJava25)
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

listOf(
    Triple("jmhJni", "JNI", jmhSqlite3ClassesJava11),
    Triple("jmhFfm", "FFM", jmhSqlite3ClassesJava25)
).forEach { (taskName, backend, sqliteClasses) ->
    tasks.register<JavaExec>(taskName) {
        val runtimeVersion = if (backend == "JNI") 11 else 25
        val resultFile = layout.buildDirectory.file("results/$taskName/results.json")
        description = "Runs the JMH benchmarks using the $backend SQLite backend"
        group = "benchmark"
        val jmhJar = tasks.named("jmhJar").get().outputs.files.singleFile
        classpath = sqliteClasses + files(jmhJar)
        mainClass.set("org.openjdk.jmh.Main")
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(runtimeVersion))
        })
        dependsOn("jmhJar", "buildHostSQLite")
        outputs.upToDateWhen { false }
        outputs.cacheIf { false }
        outputs.file(resultFile)
        doFirst {
            resultFile.get().asFile.parentFile.mkdirs()
        }
        args("-rf", "JSON", "-rff", resultFile.get().asFile.absolutePath)
        if (runtimeVersion >= 25) {
            jvmArgs("--enable-native-access=ALL-UNNAMED", "-XX:+UseCompactObjectHeaders")
        }
        if (project.hasProperty("jmh.includes")) {
            args(project.property("jmh.includes").toString())
        }
        if (project.hasProperty("jmh.params")) {
            project.property("jmh.params").toString().split(';').forEach { entry ->
                val (key, value) = entry.split('=', limit = 2)
                args("-p", "$key=$value")
            }
        }
        if (project.hasProperty("jmh.profilers")) {
            val jmhReportsDir = layout.buildDirectory.dir("reports/jmh").get().asFile.absolutePath
            project.property("jmh.profilers").toString().split(',').forEach {
                val profiler = it.trim()
                val resolved = when {
                    profiler == "jfr" -> "jfr:dir=$jmhReportsDir"
                    profiler.startsWith("jfr:") && !profiler.contains("dir=") -> "$profiler,dir=$jmhReportsDir"
                    else -> profiler
                }
                args("-prof", resolved)
            }
        }
        val benchmarkJvmArgs = buildList {
            if (runtimeVersion >= 25) {
                add("--enable-native-access=ALL-UNNAMED")
            }
            if (project.hasProperty("jmh.tempDirectory")) {
                add("-Djava.io.tmpdir=${project.property("jmh.tempDirectory")}")
            }
        }
        if (benchmarkJvmArgs.isNotEmpty()) {
            args("-jvmArgsAppend", benchmarkJvmArgs.joinToString(" "))
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
