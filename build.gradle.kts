import java.text.DateFormat
import java.util.Date

plugins {
    `java-library`
    id("pl.allegro.tech.build.axion-release") version "1.14.3"
    id("io.freefair.lombok") version "8.14.2"
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

group = "ch.admin.bar"
version = scmVersion.version
val versions = mapOf(
    "jdbc-base" to "v2.2.11",
)

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.antlr:antlr4-runtime:4.5.2")
    implementation("ch.admin.bar:enterutilities:v2.2.5")
    implementation("ch.admin.bar:SqlParser:v2.2.4")
    //implementation("com.oracle.database.jdbc:ojdbc6:11.2.0.4")
    //implementation(fileTree("lib") { include("*.jar") })
    //implementation("com.oracle.database.jdbc:ojdbc11:23.2.0.0")
            //implementation("com.oracle.database.jdbc:ojdbc8:19.28.0.0")
    //implementation("com.oracle.database.jdbc:ojdbc11:23.9.0.25.07")
    //implementation("com.oracle.database.jdbc:ojdbc8:21.11.0.0")
    implementation("com.oracle.database.jdbc:ojdbc11-production:23.9.0.25.07")
    implementation("com.oracle.ojdbc:xdb:19.3.0.0")
    implementation("com.oracle.ojdbc:xmlparserv2:19.3.0.0")
    implementation("ch.admin.bar:jdbc-base:${versions["jdbc-base"]}")

    // test dependencies
    //testImplementation("junit:junit:4.13.1")
    testImplementation("org.hamcrest:hamcrest-core:1.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.vintage:junit-vintage-engine")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.testcontainers:testcontainers:1.19.0")
    testImplementation("org.testcontainers:oracle-xe:1.19.0")
    testImplementation(testFixtures("ch.admin.bar:jdbc-base:${versions["jdbc-base"]}"))
}

tasks.test {
    useJUnitPlatform()
    // Avoid concurrency issues with Oracle Net's internal Timer (oracle.net.nt.Clock)
    // which can throw "Timer already cancelled" when multiple workers initialize/shutdown concurrently
    maxParallelForks = 1
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    // Workaround for Oracle JDBC needing access to jdk.internal.perf on Java 17+
    // Fixes: java.lang.NoClassDefFoundError: Could not initialize class oracle.net.nt.Clock
    // Reference: Oracle JDBC on JDK 9+ may require this export for NetStat/Perf
    jvmArgs("--add-exports=java.base/jdk.internal.perf=ALL-UNNAMED")
    // Some driver paths may still reflectively touch sun.misc.*
    jvmArgs("--add-opens=java.base/sun.misc=ALL-UNNAMED")
    // Disable Oracle Net network statistics to avoid using or7acle.net.nt.Clock at all
    // Force Oracle Net to use classic IO instead of NIO (avoids TimeoutSocketChannel and Clock timers)
    jvmArgs("-Doracle.jdbc.disableMBeanRegistration=true")
    jvmArgs("-Doracle.net.networkStatistics=false")
    jvmArgs("-Doracle.net.disableOob=true")
    jvmArgs("-Doracle.net.networkStatistics=false")
    jvmArgs("-Doracle.jdbc.fanEnabled=false")
    jvmArgs("-XX:-UseContainerSupport")
    //jvmArgs("-XX:+UnlockExperimentalVMOptions")
    //jvmArgs("-XX:+UseCGroupV2")

}

tasks.withType(Jar::class) {
    manifest {
        attributes["Manifest-Version"] = "1.0"
        attributes["Created-By"] = "Hartwig Thomas, Enter AG, Rüti ZH, Switzerland; Puzzle ITC AG, Switzerland"
        attributes["Specification-Title"] = "JdbcOracle"
        attributes["Specification-Vendor"] = "Swiss Federal Archives, Berne, Switzerland"
        attributes["Implementation-Title"] = "Oracle JDBC Wrapper"
        attributes["Implementation-Version"] = archiveVersion
        attributes["Implementation-Vendor"] = "Swiss Federal Archives, Berne, Switzerland"
        attributes["Built-Date"] = DateFormat.getDateInstance().format(Date())
    }
}
