import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    kotlin("jvm")
    kotlin("plugin.spring") // open classes for @Service/@Transactional proxies
    kotlin("plugin.jpa") // no-arg constructors for @Entity
    `java-test-fixtures`
    // No id("org.springframework.boot"): this is a library and must produce a plain jar.
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    api(project(":libraries:common"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-kafka") // listeners, and the producer of the dead letter topic
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-jackson") // JsonMapper for outbox payloads and the Kafka listener
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("org.postgresql:postgresql")
    // No spring-boot-starter-webmvc and no springdoc here: HTTP lives in settlement-web.

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.awaitility:awaitility")
    testImplementation("io.mockk:mockk:1.14.11") // OutboxPublisherTest: a plain unit test of the publishing loop
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Shared with settlement-web tests: container configuration and data fixtures.
    testFixturesApi(platform(SpringBootPlugin.BOM_COORDINATES))
    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
    testFixturesApi("org.springframework.boot:spring-boot-testcontainers")
    testFixturesApi("org.testcontainers:testcontainers-postgresql")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
}

tasks.test {
    useJUnitPlatform()
}
