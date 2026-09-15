plugins {
    application
}

val selektVersion = providers.gradleProperty("selektVersion")
val consumerJavaVersion = providers.gradleProperty("consumerJavaVersion")
    .map(String::toInt)
    .getOrElse(25)

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
        languageVersion.set(JavaLanguageVersion.of(consumerJavaVersion))
    }
}

sourceSets {
    main {
        java.srcDir("../jdbc-smoke/src/main/java")
    }
}

application {
    mainClass.set("com.bloomberg.selekt.samples.JdbcSmokeTest")
    if (consumerJavaVersion >= 25) {
        applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
    }
}
