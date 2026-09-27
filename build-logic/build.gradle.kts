plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    google()
}

gradlePlugin {
    plugins {
        create("selektBuildLogic") {
            id = "com.bloomberg.selekt.build-logic"
            implementationClass = "SelektBuildLogicPlugin"
        }
        create("Bloomberg JMH Plugin") {
            id = "bb-jmh"
            implementationClass = "JmhPlugin"
        }
        create("selektSbomConvention") {
            id = "com.bloomberg.selekt.sbom"
            implementationClass = "SelektSbomConventionPlugin"
        }
    }
}

dependencies {
    implementation(kotlin("gradle-plugin", version = libs.kotlin.bom.get().version))
    implementation(libs.android.tools.gradle)
    implementation(libs.cyclonedx.core)
}
