plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    // No id("io.quarkus"): only notification-web is a Quarkus application. META-INF/beans.xml makes this jar a
    // bean archive, so its CDI beans are discovered by the web build.
}

val quarkusPlatformGroupId = project.property("quarkusPlatformGroupId") as String
val quarkusPlatformArtifactId = project.property("quarkusPlatformArtifactId") as String
val quarkusPlatformVersion = project.property("quarkusPlatformVersion") as String

kotlin {
    jvmToolchain(25)
    compilerOptions {
        javaParameters = true
    }
}

allOpen {
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.ws.rs.Path")
}

dependencies {
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
    api(project(":libraries:common"))

    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-reactive-pg-client")
    implementation("io.quarkus:quarkus-messaging-kafka")
    implementation("io.quarkus:quarkus-scheduler")
    implementation("io.quarkus:quarkus-rest-client") // outgoing calls to delivery-service
    implementation("io.quarkus:quarkus-rest-client-jackson")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // Quarkus 3: Jackson 2
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.4")
    // No quarkus-rest (server) and no quarkus-smallrye-openapi here: HTTP lives in notification-web.

    testImplementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.7") // not in the Quarkus BOM; same version Spring Boot uses
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
