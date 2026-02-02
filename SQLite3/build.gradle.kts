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
    "-DSQLITE_OMIT_PROGRESS_CALLBACK",
    "-DSQLITE_OMIT_SHARED_CACHE",
    "-DSQLITE_TEMP_STORE=3",
    "-DSQLITE_THREADSAFE=2",
    "-DSQLITE_TRUSTED_SCHEMA=0",
    "-DSQLITE_USE_ALLOCA",
    "-DSQLITE_USE_URI=1"
)

tasks.register<Exec>("configureSqlCipher") {
    workingDir = File("$projectDir/src/main/external/sqlcipher")
    commandLine("./configure")
    environment("CFLAGS", cFlags.joinToString(" "))
    args("--with-tempstore=yes")
    logging.captureStandardOutput(LogLevel.INFO)
}

tasks.register("amalgamate") {
    dependsOn("amalgamateSQLite", "copySQLiteHeader", "copySQLiteImplementation")
}

tasks.register<Exec>("amalgamateSQLite") {
    dependsOn("configureSqlCipher")
    workingDir = File("$projectDir/src/main/external/sqlcipher")
    commandLine("make")
    args("sqlite3.c")
}

tasks.register<Copy>("copySQLiteHeader") {
    mustRunAfter("amalgamateSQLite")
    from("$projectDir/src/main/external/sqlcipher")
    include("sqlite3.h")
    into("$projectDir/sqlite3/generated/include/sqlite3")
}

tasks.register<Copy>("copySQLiteImplementation") {
    mustRunAfter("amalgamateSQLite")
    from("$projectDir/src/main/external/sqlcipher")
    include("sqlite3.c")
    into("$projectDir/sqlite3/generated/cpp")
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
    args(
        "-DCMAKE_BUILD_TYPE=Release",
        "-DUSE_CCACHE=1",
        "-DSLKT_TARGET_ABI=${platformIdentifier()}",
        projectDir
    )
}

tasks.register<Exec>("makeSQLite") {
    dependsOn("cmakeSQLite")
    workingDir(".cxx-host")
    commandLine("make", "selekt")
}

fun osName() = System.getProperty("os.name").lowercase(Locale.US).run {
    when {
        startsWith("mac") -> "darwin"
        startsWith("windows") -> "windows"
        else -> replace("\\s+", "_")
    }
}

fun platformIdentifier() = "${osName()}-${System.getProperty("os.arch")}"
logger.quiet("Resolved platform identifier: {}", platformIdentifier())

tasks.register<Copy>("buildHost") {
    dependsOn("makeSQLite")
    from(".cxx-host/sqlite3")
    into(layout.buildDirectory.dir("intermediates/libs/${platformIdentifier()}"))
    include("*.dll", "*.dylib", "*.so")
}

tasks.register<Exec>("cleanSqlCipher") {
    workingDir = File("$projectDir/src/main/external/sqlcipher")
    commandLine("git")
    args("clean", "-dfx")
}

tasks.register<Delete>("deleteCxxHost") {
    delete(".cxx-host")
}

tasks.named("clean") {
    dependsOn("cleanSqlCipher", "deleteCxxHost")
}
