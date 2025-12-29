plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
}

group = "com.siba"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // Complete docx4j stack
    implementation("org.docx4j:docx4j-core:11.5.5")
    implementation("org.docx4j:docx4j-JAXB-ReferenceImpl:11.5.5")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Add SLF4J implementation to fix logging warnings
    implementation("org.slf4j:slf4j-simple:2.0.9")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}