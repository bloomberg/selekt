plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("selektSbomConvention") {
            id = "com.bloomberg.selekt.sbom"
            implementationClass = "SelektSbomConventionPlugin"
        }
    }
}

dependencies {
    implementation("org.cyclonedx:cyclonedx-core-java:13.1.0")
}
