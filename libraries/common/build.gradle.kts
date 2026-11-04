import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(kotlin("stdlib"))
    // No Jackson in main: the contracts are used by Spring (Jackson 3) and Quarkus (Jackson 2) services.

    // The round-trip test runs both Jackson lines the consumers use; versions from their BOMs.
    testImplementation(platform(SpringBootPlugin.BOM_COORDINATES))
    testImplementation("tools.jackson.module:jackson-module-kotlin")
    testImplementation(
        enforcedPlatform(
            "${project.property("quarkusPlatformGroupId")}:${project.property("quarkusPlatformArtifactId")}:" +
                "${project.property("quarkusPlatformVersion")}",
        ),
    )
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
