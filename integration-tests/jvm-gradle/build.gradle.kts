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
    implementation("com.bloomberg.selekt:selekt-jvm")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(consumerJavaVersion))
    }
}

sourceSets {
    main {
        java.srcDir("../consumer-smoke/src/main/java")
    }
}

application {
    mainClass.set("com.bloomberg.selekt.samples.JvmSmokeTest")
    if (consumerJavaVersion >= 25) {
        applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
    }
}
