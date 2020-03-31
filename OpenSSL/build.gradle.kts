@file:UseExperimental(ExperimentalStdlibApi::class)

import java.util.Locale

val archive = fileTree("$projectDir/src/main/external/").matching { include("*.tar.gz") }.min()!!

tasks.register<Copy>("unpack") {
    from(tarTree(archive))
    into("$buildDir/generated/")
}

arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64").forEach {
    tasks.register<Exec>("assemble${it.capitalize(Locale.ROOT)}") {
        dependsOn("unpack")
        workingDir(projectDir)
        commandLine("./build_libraries.sh")
        args(
            archive.run { File("$buildDir/generated/${name.substringBefore(".tar.gz")}") }.path,
            it,
            21
        )
    }
}

fun osName() = System.getProperty("os.name").toLowerCase(Locale.US).run {
    when {
        startsWith("mac") -> "darwin"
        startsWith("windows") -> "windows"
        else -> replace("\\s+", "_")
    }
}

fun platformIdentifier() = "${osName()}-${System.getProperty("os.arch")}"

val openSSLWorkindDir = archive.run { File("$buildDir/generated/${name.substringBefore(".tar.gz")}") }.path

tasks.register<Exec>("configureHost") {
    dependsOn("unpack")
    workingDir(openSSLWorkindDir)
    commandLine("./config")
}

tasks.register<Exec>("makeHost") {
    dependsOn("configureHost")
    workingDir(openSSLWorkindDir)
    commandLine("make")
    args("build_libs")
}

tasks.register<Task>("assembleHost") {
    dependsOn("makeHost")
    doLast {
        "${buildDir.path}/libs/${platformIdentifier()}".let {
            mkdir(it)
            copy {
                logger.quiet("Copying to: $it")
                from(fileTree(openSSLWorkindDir) {
                    arrayOf(".a").forEach { e ->
                        include("**/libcrypto$e")
                    }
                }.files)
                into(it)
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(buildDir)
}
