plugins {
    application
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin

    id("com.autonomousapps.dependency-analysis") version "3.6.1"
    id("com.gradleup.shadow") version "9.3.2"
}

group = "de.siba"
version = "1.2.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.docx4j.core)
    runtimeOnly(libs.docx4j.jaxb)

    // Add SLF4J implementation to fix logging warnings
    runtimeOnly(libs.slf4j)

    implementation(libs.kotlinx.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.clikt)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test.junit)
    testRuntimeOnly(libs.junit.platform)
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "de.siba.reportbookgen.ReportBookGeneratorKt"
}

tasks.withType<Test> {
    useJUnitPlatform()
}