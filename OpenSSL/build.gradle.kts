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

import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import java.util.Locale

repositories {
    mavenCentral()
}

plugins {
    base
    id("de.undercouch.download") version Versions.GRADLE_DOWNLOAD_TASK_PLUGIN.version
}

fun Project.openSslVersion() = arrayOf(
    "${property("openssl.version")}",
    "${property("openssl.version.suffix")}"
).joinToString("")

val openSslBaseUrl = "https://www.openssl.org/source"
fun Project.openSslUrl() = "$openSslBaseUrl/openssl-${openSslVersion()}.tar.gz"
fun Project.openSslPgpUrl() = "$openSslBaseUrl/openssl-${openSslVersion()}.tar.gz.asc"

val archive = File("$buildDir/tmp/openssl-${openSslVersion()}.tar.gz")
val archivePgp = File("${archive.path}.asc")

tasks.register<Download>("downloadOpenSslPgp") {
    val url = openSslPgpUrl()
    src(url)
    dest(archivePgp)
    overwrite(false)
    inputs.property("url", url)
    outputs.files(archivePgp)
    finalizedBy("verifyOpenSslPgpChecksum")
    mustRunAfter("clean")
}

tasks.register<Download>("downloadOpenSsl") {
    val url = openSslUrl()
    src(url)
    dest(archive)
    overwrite(false)
    inputs.property("url", url)
    outputs.files(archive)
    dependsOn("downloadOpenSslPgp")
    finalizedBy("verifyOpenSslChecksum", "verifyOpenSslSignature")
    mustRunAfter("clean")
}

tasks.register<Verify>("verifyOpenSslPgpChecksum") {
    src(archivePgp)
    algorithm("SHA-256")
    checksum("${project.property("openssl.pgp.sha256")}")
}

tasks.register<Verify>("verifyOpenSslChecksum") {
    src(archive)
    algorithm("SHA-256")
    checksum("${project.property("openssl.sha256")}")
}

tasks.register<Exec>("verifyOpenSslSignature") {
    inputs.files(archivePgp, archive)
    commandLine(
        "gpg", "--no-default-keyring", "--keyring", "$projectDir/openssl.gpg", "--verify",
        archivePgp, archive
    )
}

fun openSslWorkingDir(target: String) = archive.run {
    File("$buildDir/generated/$target/${name.substringBefore(".tar.gz")}")
}.path

arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64").forEach {
    val titleCaseName = it.replaceFirstChar { c -> c.uppercaseChar() }
    tasks.register<Copy>("unpackOpenSsl$titleCaseName") {
        from(tarTree(archive))
        into("$buildDir/generated/$it")
        dependsOn("downloadOpenSsl")
    }

    tasks.register<Exec>("assemble$titleCaseName") {
        dependsOn("unpackOpenSsl$titleCaseName")
        inputs.file("$projectDir/build_libraries.sh")
        inputs.property("version", openSslVersion())
        outputs.files(fileTree("${openSslWorkingDir(it)}/include") { include("**/*.h") })
            .withPropertyName("headers")
        outputs.dir("$buildDir/libs/$it").withPropertyName("lib")
        outputs.cacheIf { true }
        workingDir(projectDir)
        commandLine("./build_libraries.sh")
        args(
            archive.run { File("$buildDir/generated/$it/${name.substringBefore(".tar.gz")}") }.path,
            it,
            21
        )
        logging.captureStandardOutput(LogLevel.INFO)
    }
}

tasks.register("assembleAndroid") {
    arrayOf("Armeabi-v7a", "Arm64-v8a", "X86", "X86_64").forEach {
        dependsOn("assemble$it")
    }
}

fun osName() = System.getProperty("os.name").lowercase(Locale.US).run {
    when {
        startsWith("mac") -> "darwin"
        startsWith("windows") -> "windows"
        else -> replace("\\s+", "_")
    }
}

fun targetIdentifier() = "${osName()}-${System.getProperty("os.arch")}"

val openSslWorkingDir: String = openSslWorkingDir(targetIdentifier())

// FIXME Some of the host building logic parallels Android's above. Re-purpose?
tasks.register<Copy>("unpackOpenSslHost") {
    from(tarTree(archive))
    into("$buildDir/generated/${targetIdentifier()}")
    dependsOn("downloadOpenSsl")
    mustRunAfter("clean")
}

tasks.register<Exec>("configureHost") {
    dependsOn("unpackOpenSslHost")
    inputs.property("target", targetIdentifier())
    inputs.property("version", openSslVersion())
    outputs.files("$openSslWorkingDir/Makefile", "$openSslWorkingDir/configdata.pm")
        .withPropertyName("configure")
    outputs.cacheIf { true }
    workingDir(openSslWorkingDir)
    commandLine("./config")
}

tasks.register<Exec>("makeHost") {
    dependsOn("configureHost")
    inputs.property("target", targetIdentifier())
    inputs.property("version", openSslVersion())
    arrayOf(".a").forEach {
        outputs.files("$openSslWorkingDir/libcrypto$it")
            .withPropertyName("libcrypto$it")
    }
    outputs.files(fileTree("$openSslWorkingDir/include") { include("**/*.h") })
        .withPropertyName("headers")
    workingDir(openSslWorkingDir)
    outputs.cacheIf { true }
    commandLine("make")
    args("build_libs")
    logging.captureStandardOutput(LogLevel.INFO)
}

tasks.register<Task>("assembleHost") {
    dependsOn("makeHost")
    inputs.property("target", targetIdentifier())
    inputs.property("version", openSslVersion())
    val outputDir = "$buildDir/libs/${targetIdentifier()}"
    outputs.dir(outputDir).withPropertyName("libs")
    outputs.cacheIf { true }
    doLast {
        outputDir.let {
            mkdir(it)
            copy {
                logger.quiet("Copying to: $it")
                from(fileTree(openSslWorkingDir) {
                    arrayOf(".a").forEach { e ->
                        include("**/libcrypto$e")
                    }
                }.files)
                into(it)
            }
        }
    }
}
