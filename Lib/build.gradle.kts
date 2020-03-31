plugins {
    kotlin("jvm")
}

description = "Selekt core library."

dependencies {
    implementation(selekt("api", selektVersionName))
    implementation(selekt("commons", selektVersionName))
    implementation(selekt("pools", selektVersionName))
    implementation(selekt("sqlite3", selektVersionName))
}

disableKotlinCompilerAssertions()

tasks.register("assembleSelekt") {
    dependsOn("assemble")
}
