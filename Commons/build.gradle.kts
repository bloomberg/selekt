plugins {
    kotlin("jvm")
}

description = "Selekt Commons library."

disableKotlinCompilerAssertions()

tasks.register("assembleSelekt") {
    dependsOn("assemble")
}
