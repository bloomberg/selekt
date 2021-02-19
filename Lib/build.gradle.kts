/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

repositories {
    mavenCentral()
    jcenter()
}

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    jacoco
    `maven-publish`
}

description = "Selekt core library."

disableKotlinCompilerAssertions()

java {
    @Suppress("UnstableApiUsage")
    withSourcesJar()
}

dependencies {
    compileOnly(selekt("annotations", selektVersionName))
    implementation(selekt("api", selektVersionName))
    implementation(selekt("sqlite3", selektVersionName))
}

tasks.register("assembleSelekt") {
    dependsOn("assemble")
    dependsOn("sourcesJar")
}

publishing {
    publications.register<MavenPublication>("main") {
        groupId = selektGroupId
        artifactId = "selekt-java"
        version = selektVersionName
        from(components.getByName("java"))
        pom { commonInitialisation(project) }
    }
}
