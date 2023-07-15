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

import java.util.Locale

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm")
    `maven-publish`
    signing
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

disableKotlinCompilerAssertions()

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications.register<MavenPublication>("main") {
        from(components.getByName("java"))
        pom {
            commonInitialisation(project)
            description.set("Selekt Java SQLite interface library.")
        }
    }
}

tasks.register<Exec>("configureSqlCipher") {
    workingDir = File("$projectDir/src/main/external/sqlcipher")
    commandLine("./configure")
    environment("CFLAGS", cFlags.joinToString(" "))
    args(
        "--enable-tempstore=yes",
        "--with-crypto-lib=none"
    )
    logging.captureStandardOutput(LogLevel.INFO)
}

tasks.register<Exec>("amalgamate") {
    dependsOn("configureSqlCipher")
    workingDir = File("$projectDir/src/main/external/sqlcipher")
    commandLine("make")
    args("sqlite3.c")
    doLast {
        copy {
            from(workingDir)
            include("sqlite3.c")
            into("$projectDir/sqlite3/generated/cpp")
        }
        copy {
            from(workingDir)
            include("sqlite3.h")
            into("$projectDir/sqlite3/generated/include/sqlite3")
        }
    }
}

tasks.register<Exec>("cmakeSQLite") {
    dependsOn(":OpenSSL:assembleHost", "amalgamate")
    workingDir(".cxx-host")
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

tasks.register<Task>("buildHost") {
    dependsOn("makeSQLite")
    doLast {
        "${buildDir.path}/intermediates/libs/${platformIdentifier()}".let {
            mkdir(it)
            copy {
                logger.quiet("Copying to: $it")
                from(fileTree(".cxx-host") {
                    include("**/*.dll", "**/*.dylib", "**/*.so")
                }.files)
                into(it)
            }
        }
    }
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
