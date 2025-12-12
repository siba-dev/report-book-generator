plugins {
    kotlin("jvm") version libs.versions.kotlin
    application

    kotlin("plugin.serialization") version libs.versions.kotlin

    id("com.autonomousapps.dependency-analysis") version "3.5.1"
}

group = "de.siba"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.docx4j.core)
    runtimeOnly(libs.docx4j.jaxb)

    // Add SLF4J implementation to fix logging warnings
    runtimeOnly(libs.slf4j)

    implementation(libs.kotlin.json)
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "de.siba.repotbookgen.ReportBookGeneratorKt"
}