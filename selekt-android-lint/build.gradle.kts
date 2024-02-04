/*
 * Copyright 2022 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

repositories {
    mavenCentral()
    google()
}

plugins {
    kotlin("jvm")
    `maven-publish`
    signing
    id("io.gitlab.arturbosch.detekt")
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnly("com.android.tools.lint:lint:${Versions.ANDROID_LINT}")
    compileOnly("com.android.tools.lint:lint-api:${Versions.ANDROID_LINT}")
    implementation(kotlin("reflect", Versions.KOTLIN.version))
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Lint-Registry-v2"] = "com.bloomberg.selekt.android.lint.SelektIssueRegistry"
    }
}

publishing {
    publications.register<MavenPublication>("main") {
        from(components.getByName("java"))
        pom {
            commonInitialisation(project)
            description.set("Selekt Android Lint library.")
        }
    }
}
