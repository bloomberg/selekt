plugins {
    id("com.android.library")
    signing
    `maven-publish`
}

repositories {
    mavenCentral()
    google()
    jcenter()
}

fun Project.abis() = findProperty("selekt.abis")?.toString()?.split(",")
    ?: listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

android {
    compileSdkVersion(Versions.ANDROID_SDK.version.toInt())
    buildToolsVersion(Versions.ANDROID_BUILD_TOOLS.version)
    ndkVersion = Versions.ANDROID_NDK.version

    defaultConfig {
        minSdkVersion(21)
        versionCode = 1
        versionName = sqlcipherVersionName

        ndk {
            abiFilters.addAll(abis())
        }
    }

    externalNativeBuild {
        cmake {
            path("$rootDir/SQLite3/CMakeLists.txt")
            version = Versions.CMAKE.version
        }
    }
}

tasks.register("assembleSelekt") {
    dependsOn("assembleRelease")
}

afterEvaluate {
    publishing {
        publications.create<MavenPublication>("main") {
            groupId = selektGroupId
            artifactId = "selekt-android-sqlcipher"
            version = sqlcipherVersionName
            from(components["release"])
            pom {
                commonInitialisation(project)
                description.set("SQLCipher for Selekt's Android Library.")
                licenses {
                    license {
                        name.set("Dual OpenSSL and SSLeay License")
                        url.set("https://www.openssl.org/source/license-openssl-ssleay.txt")
                    }
                    license {
                        name.set("Zetetic LLC")
                        url.set("https://www.zetetic.net/sqlcipher/license")
                    }
                }
            }
        }.also {
            signing { sign(it) }
        }
    }
}
