plugins {
    application
}

val selektVersion = providers.gradleProperty("selektVersion")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(platform("com.bloomberg.selekt:selekt-bom:${selektVersion.get()}"))
    implementation("com.bloomberg.selekt:selekt-jdbc")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

sourceSets {
    main {
        java.srcDir("../jdbc-smoke/src/main/java")
    }
}

application {
    mainClass.set("com.bloomberg.selekt.samples.JdbcSmokeTest")
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}
