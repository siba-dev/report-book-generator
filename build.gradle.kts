plugins {
    kotlin("jvm") version "2.2.0"
}

group = "com.siba"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.docx4j:docx4j-core:11.4.9")

    // Add docx4j JAXB Reference Implementation - this contains the missing NamespacePrefixMapper
    implementation("org.docx4j:docx4j-JAXB-ReferenceImpl:11.4.9")

    // Add SLF4J implementation to fix logging warnings
    implementation("org.slf4j:slf4j-simple:2.0.9")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}