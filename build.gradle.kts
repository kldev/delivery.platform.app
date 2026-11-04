// Plugins are declared here once, with their versions, and applied only by the projects that need them.
// The root project has no code: nothing is applied to it (it used to apply Spring Boot, so
// `./gradlew build` failed on a root bootJar without a main class).
plugins {
    kotlin("jvm") version "2.4.0" apply false
    kotlin("plugin.spring") version "2.4.0" apply false
    kotlin("plugin.allopen") version "2.4.0" apply false
    kotlin("plugin.jpa") version "2.4.0" apply false // was 2.4.10: one Kotlin version for all its plugins
    id("io.quarkus") version "3.40.1" apply false
    id("org.springframework.boot") version "4.1.0" apply false
}

allprojects {
    group = "com.pl.platform"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
