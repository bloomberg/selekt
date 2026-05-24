/*
 * Copyright 2020 Bloomberg Finance L.P.
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

import java.nio.file.Files
import java.nio.file.Paths
import java.util.Locale
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    base
    java
}

repositories {
    mavenCentral()
}

val cFlags = arrayOf(
    "-DHAVE_USLEEP=1",
    "-DSQLCIPHER_CRYPTO_OPENSSL",
    "-DSQLITE_ALLOW_URI_AUTHORITY",
    "-DSQLITE_DEFAULT_AUTOVACUUM=2",
    "-DSQLITE_DEFAULT_CACHE_SIZE=200",
    "-DSQLITE_DEFAULT_FILE_PERMISSIONS=0600",
    "-DSQLITE_DEFAULT_MEMSTATUS=0",
    "-DSQLITE_DEFAULT_SYNCHRONOUS=2",
    "-DSQLITE_DEFAULT_WAL_SYNCHRONOUS=1",
    "-DSQLITE_DQS=0",
    "-DSQLITE_ENABLE_BATCH_ATOMIC_WRITE",
    "-DSQLITE_ENABLE_FTS3_PARENTHESIS",
    "-DSQLITE_ENABLE_FTS4",
    "-DSQLITE_ENABLE_FTS5",
    "-DSQLITE_ENABLE_JSON1",
    "-DSQLITE_ENABLE_MATH_FUNCTIONS",
    "-DSQLITE_ENABLE_MEMORY_MANAGEMENT=1",
    "-DSQLITE_ENABLE_RTREE",
    "-DSQLITE_ENABLE_STAT4",
    "-DSQLITE_ENABLE_UNLOCK_NOTIFY",
    "-DSQLITE_EXTRA_INIT=sqlcipher_extra_init",
    "-DSQLITE_EXTRA_SHUTDOWN=sqlcipher_extra_shutdown",
    "-DSQLITE_HAS_CODEC=1",
    "-DSQLITE_LIKE_DOESNT_MATCH_BLOBS",
    "-DSQLITE_OMIT_AUTOINIT",
    "-DSQLITE_OMIT_DECLTYPE",
    "-DSQLITE_OMIT_DEPRECATED",
    "-DSQLITE_OMIT_LOAD_EXTENSION",
    "-DSQLITE_OMIT_SHARED_CACHE",
    "-DSQLITE_TEMP_STORE=3",
    "-DSQLITE_THREADSAFE=2",
    "-DSQLITE_TRUSTED_SCHEMA=0",
    "-DSQLITE_USE_ALLOCA",
    "-DSQLITE_USE_URI=1"
)

fun osName() = System.getProperty("os.name").lowercase(Locale.US).run {
    when {
        startsWith("mac") -> "darwin"
        startsWith("windows") -> "windows"
        else -> replace("\\s+", "_")
    }
}

fun targetIdentifier(): String = "${osName()}-${System.getProperty("os.arch")}".let {
    if (System.getenv("SELEKT_LIBC") == "musl") { "$it-musl" } else { it }
}
logger.quiet("Resolved platform identifier: {}", targetIdentifier())

val isWindows = osName() == "windows"

val vec1Enabled: String = providers.gradleProperty("selekt.vec1.enabled")
    .orElse(providers.environmentVariable("SELEKT_ENABLE_VEC1"))
    .orElse("ON")
    .get()
val vec1EnableX86Avx2: String = providers.gradleProperty("selekt.vec1.enableX86Avx2")
    .orElse(providers.environmentVariable("SELEKT_VEC1_ENABLE_X86_AVX2"))
    .orElse("OFF")
    .get()

val sqlcipherDir = file("src/main/external/sqlcipher")
val generatedCppDir = file("sqlite3/generated/cpp")
val generatedIncludeDir = file("sqlite3/generated/include/sqlite3")

tasks.register<Exec>("configureSqlCipher") {
    workingDir = sqlcipherDir
    inputs.files(fileTree(sqlcipherDir) { include("configure", "configure.ac", "Makefile.in") })
    inputs.property("cFlags", cFlags.joinToString(" "))
    outputs.files("$sqlcipherDir/Makefile", "$sqlcipherDir/config.status")
    if (isWindows) {
        commandLine("cmd", "/c", "echo", "Skipping configure on Windows (using Makefile.msc)")
    } else {
        commandLine("./configure")
        environment("CFLAGS", cFlags.joinToString(" "))
        args("--with-tempstore=yes")
    }
    logging.captureStandardOutput(LogLevel.INFO)
}

tasks.register("amalgamate") {
    dependsOn("amalgamateSQLite", "copySQLiteHeader", "copySQLiteImplementation")
}

tasks.register<Exec>("amalgamateSQLite") {
    dependsOn("configureSqlCipher")
    workingDir = sqlcipherDir
    inputs.dir("$sqlcipherDir/src")
    inputs.property("cFlags", cFlags.joinToString(" "))
    outputs.files("$sqlcipherDir/sqlite3.c", "$sqlcipherDir/sqlite3.h")
    if (isWindows) {
        commandLine("nmake", "/f", "Makefile.msc", "sqlite3.c", "OPTS=${cFlags.joinToString(" ")}")
    } else {
        commandLine("make")
        args("sqlite3.c")
    }
}

tasks.register<Copy>("copySQLiteHeader") {
    mustRunAfter("amalgamateSQLite")
    from(sqlcipherDir)
    include("sqlite3.h")
    into(generatedIncludeDir)
}

tasks.register<Copy>("copySQLiteImplementation") {
    mustRunAfter("amalgamateSQLite")
    from(sqlcipherDir)
    include("sqlite3.c")
    into(generatedCppDir)
}

tasks.register<Exec>("cmakeSQLite") {
    dependsOn(":OpenSSL:assembleHost", "amalgamate")
    val workingDir = Paths.get("$projectDir/.cxx-host")
    doFirst {
        Files.createDirectories(workingDir)
    }
    workingDir(".cxx-host")
    val javaToolchainService = project.extensions.getByType(JavaToolchainService::class.java)
    val javaLauncher = javaToolchainService.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    }.get()
    environment("JAVA_HOME", javaLauncher.metadata.installationPath.asFile.absolutePath)
    commandLine("cmake")
    val cmakeArgs = mutableListOf(
        "-DCMAKE_BUILD_TYPE=Release",
        "-DCMAKE_VERBOSE_MAKEFILE=ON",
        "-DUSE_CCACHE=1",
        "-DSLKT_TARGET_ABI=${targetIdentifier()}",
        "-DSELEKT_ENABLE_VEC1=$vec1Enabled",
        "-DSELEKT_VEC1_ENABLE_X86_AVX2=$vec1EnableX86Avx2"
    )
    if (isWindows) {
        cmakeArgs.addAll(0, listOf("-G", "MinGW Makefiles"))
        cmakeArgs.add("-DCMAKE_C_COMPILER=gcc")
        cmakeArgs.add("-DCMAKE_CXX_COMPILER=g++")
        val prefixPath = System.getenv("CMAKE_PREFIX_PATH")
        if (!prefixPath.isNullOrBlank()) {
            cmakeArgs.add("-DCMAKE_PREFIX_PATH=$prefixPath")
        }
    }
    cmakeArgs.add(projectDir.absolutePath)
    args(cmakeArgs)
}

tasks.register<Exec>("makeSQLite") {
    dependsOn("cmakeSQLite")
    workingDir(".cxx-host")
    commandLine("cmake", "--build", ".", "--target", "selekt", "--verbose")
}


tasks.register<Copy>("buildHost") {
    dependsOn("makeSQLite")
    from(".cxx-host/sqlite3")
    into(layout.buildDirectory.dir("intermediates/libs/${targetIdentifier()}"))
    include("*.dll", "*.dylib", "*.so")
}

tasks.register<Exec>("cleanSqlCipher") {
    workingDir = sqlcipherDir
    commandLine("git")
    args("clean", "-dfx")
}

tasks.register<Delete>("deleteCxxHost") {
    delete(".cxx-host")
}

tasks.named("clean") {
    dependsOn("cleanSqlCipher", "deleteCxxHost")
}
