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

import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import java.util.Locale
import java.util.Properties

plugins {
    base
    alias(libs.plugins.undercouch.download)
}

repositories {
    mavenCentral()
}

val opensslProperties = Properties().apply {
    file("openssl.properties").inputStream().use(::load)
}

fun Project.openSslVersion() = opensslProperties.getProperty("openssl.version")

val openSslBaseUrl = "https://www.openssl.org/source"
fun Project.openSslUrl() = "$openSslBaseUrl/openssl-${openSslVersion()}.tar.gz"
fun Project.openSslPgpUrl() = "$openSslBaseUrl/openssl-${openSslVersion()}.tar.gz.asc"

val archive: Provider<RegularFile> = layout.buildDirectory.file("tmp/openssl-${openSslVersion()}.tar.gz")
val archivePgp: Provider<RegularFile> = layout.buildDirectory.file("tmp/openssl-${openSslVersion()}.tar.gz.asc")

tasks.register<Download>("downloadOpenSslPgp") {
    val url = openSslPgpUrl()
    src(url)
    dest(archivePgp)
    onlyIfModified(true)
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
    src(archivePgp.get().asFile)
    algorithm("SHA-256")
    checksum(opensslProperties.getProperty("openssl.pgp.sha256"))
}

tasks.register<Verify>("verifyOpenSslChecksum") {
    src(archive.get().asFile)
    algorithm("SHA-256")
    checksum(opensslProperties.getProperty("openssl.sha256"))
}

tasks.register<Exec>("verifyOpenSslSignature") {
    inputs.files(archivePgp, archive)
    val gpgHome = layout.buildDirectory.dir("tmp/gpg-home").get().asFile
    val keyboxSource = file("openssl.gpg")
    val gpgHomePath = gpgHome.absolutePath.toMsysPath()
    val pgpPath = archivePgp.get().asFile.absolutePath.toMsysPath()
    val archivePath = archive.get().asFile.absolutePath.toMsysPath()
    doFirst {
        gpgHome.mkdirs()
        gpgHome.resolve("trustdb.gpg").createNewFile()
        keyboxSource.copyTo(gpgHome.resolve("pubring.kbx"), overwrite = true)
    }
    commandLine(
        "gpg", "--homedir", gpgHomePath,
        "--verify", pgpPath, archivePath
    )
}

fun String.toMsysPath(): String = if (length >= 2 && this[1] == ':') {
    "/${this[0].lowercaseChar()}${substring(2).replace('\\', '/')}"
} else {
    this
}

fun openSslWorkingDir(target: String): Provider<Directory> = archive.run {
    layout.buildDirectory.dir("generated/$target/${get().asFile.name.substringBefore(".tar.gz")}")
}

arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64").forEach {
    val titleCaseName = it.replaceFirstChar { c -> c.uppercaseChar() }
    tasks.register<Copy>("unpackOpenSsl$titleCaseName") {
        from(tarTree(archive))
        into(layout.buildDirectory.dir("generated/$it"))
        dependsOn("downloadOpenSsl")
    }

    tasks.register<Exec>("assemble$titleCaseName") {
        dependsOn("unpackOpenSsl$titleCaseName")
        inputs.file("$projectDir/build_libraries.sh")
        inputs.property("version", openSslVersion())
        outputs.files(fileTree("${openSslWorkingDir(it)}/include") { include("**/*.h") })
            .withPropertyName("headers")
        outputs.dir(layout.buildDirectory.dir("libs/$it")).withPropertyName("lib")
        outputs.cacheIf { true }
        workingDir(projectDir)
        commandLine("./build_libraries.sh")
        args(
            archive.run { layout.buildDirectory.file("generated" +
                "/$it/${get().asFile.name.substringBefore(".tar.gz")}")
            }.get().asFile.path,
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

fun targetIdentifier() = "${osName()}-${System.getProperty("os.arch")}".let {
    if (System.getenv("SELEKT_LIBC") == "musl") { "$it-musl" } else { it }
}

val openSslWorkingDir: Provider<Directory> = openSslWorkingDir(targetIdentifier())

// FIXME Some of the host building logic parallels Android's above. Re-purpose?
tasks.register<Copy>("unpackOpenSslHost") {
    from(tarTree(archive))
    into(layout.buildDirectory.dir("generated/${targetIdentifier()}"))
    dependsOn("downloadOpenSsl")
    mustRunAfter("clean")
}

tasks.register<Exec>("configureHost") {
    dependsOn("unpackOpenSslHost")
    inputs.property("target", targetIdentifier())
    inputs.property("version", openSslVersion())
    val openSslWorkingDir = openSslWorkingDir.get().asFile
    workingDir(openSslWorkingDir)
    outputs.files("$openSslWorkingDir/Makefile", "$openSslWorkingDir/configdata.pm")
        .withPropertyName("configure")
    outputs.cacheIf { false }
    val configArgs = listOf(
        "-fPIC",
        "-fstack-protector-all",
        "no-idea", "no-camellia", "no-seed", "no-bf", "no-blake2", "no-cast", "no-rc2", "no-rc4", "no-rc5",
        "no-md2", "no-rmd160", "no-md4", "no-ecdh", "no-sock",
        "no-ssl", "no-ssl3", "no-ssl3-method",
        "no-tls", "no-tls1", "no-tls1-method", "no-tls1_1", "no-tls1_1-method", "no-tls1_2", "no-tls1_2-method", "no-tls1_3",
        "no-chacha", "no-des", "no-dh", "no-dsa", "no-ec", "no-ecdsa", "no-ec2m", "no-ocb",
        "no-dtls", "no-nextprotoneg", "no-poly1305", "no-rfc3779", "no-whirlpool",
        "no-scrypt", "no-srp", "no-mdc2", "no-engine", "no-ts", "no-sse2", "no-sm2", "no-sm3",
        "no-sm4", "no-ocsp", "no-cmac", "no-srtp", "no-shared", "no-comp", "no-ct", "no-cms",
        "no-capieng", "no-deprecated", "no-autoerrinit", "no-stdio", "no-ui-console", "no-filenames"
    )
    if (osName() == "windows") {
        val msys2Bash = "C:/msys64/usr/bin/bash.exe"
        val workDir = openSslWorkingDir.absolutePath.replace('\\', '/')
        commandLine(msys2Bash, "-l", "-c",
            "cd '$workDir' && perl Configure mingw64 ${configArgs.joinToString(" ")}")
    } else {
        commandLine("./config")
        args(configArgs)
    }
}

tasks.register<Exec>("makeHost") {
    dependsOn("configureHost")
    inputs.property("target", targetIdentifier())
    inputs.property("version", openSslVersion())
    workingDir(openSslWorkingDir)
    val openSslWorkingDir = openSslWorkingDir.get().asFile
    arrayOf(".a").forEach {
        outputs.files("$openSslWorkingDir/libcrypto$it")
            .withPropertyName("libcrypto$it")
    }
    outputs.files(fileTree("$openSslWorkingDir/include") { include("**/*.h") })
        .withPropertyName("headers")
    outputs.cacheIf { false }
    if (osName() == "windows") {
        environment("PATH", "C:\\msys64\\usr\\bin;${System.getenv("PATH")}")
        commandLine("mingw32-make")
    } else {
        commandLine("make")
    }
    args("build_libs")
    logging.captureStandardOutput(LogLevel.INFO)
}

tasks.register<Copy>("assembleHost") {
    dependsOn("makeHost")
    inputs.property("target", targetIdentifier())
    inputs.property("version", openSslVersion())
    val outputDir = layout.buildDirectory.dir("libs/${targetIdentifier()}")
    outputs.dir(outputDir).withPropertyName("libs")
    outputs.cacheIf { false }
    val openSslWorkingDir = openSslWorkingDir.get().asFile
    from(fileTree(openSslWorkingDir) {
        arrayOf(".a").forEach { e ->
            include("**/libcrypto$e")
        }
    })
    into(outputDir)
}
