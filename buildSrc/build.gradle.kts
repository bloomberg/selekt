import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    jcenter()
}

gradlePlugin {
    plugins {
        create("Selekt Plugin") {
            id = "selekt"
            implementationClass = "SelektPlugin"
        }
    }
}

dependencies {
    compileOnly(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("gradle-plugin"))
    implementation("com.android.tools.build:gradle:4.0.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=org.mylibrary.ExperimentalMarker"
}
