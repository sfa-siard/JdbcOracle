/*
 * Build file for JdbcOracle Gradle build.
 *
 * This file configures the build for the JdbcOracle project.
 */


plugins {
    `java-library`
    id("pl.allegro.tech.build.axion-release") version "1.14.3"
    id("io.freefair.lombok") version "6.5.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

version = scmVersion.version

repositories {
    mavenCentral()
    flatDir {
        dirs("lib")
    }
}

dependencies {
    implementation("org.antlr:antlr4-runtime:4.5.2")

    implementation(fileTree("lib") { include("*.jar") })

    testImplementation("junit:junit:4.13.1")
    testImplementation("org.hamcrest:hamcrest-core:1.3")

    //necessary for useJUnitPlatform()
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.vintage:junit-vintage-engine")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    // testcontainers
    testImplementation("org.testcontainers:testcontainers:1.19.0")
    testImplementation("org.testcontainers:oracle-xe:1.19.0")
}

tasks.test {
    useJUnitPlatform()
}