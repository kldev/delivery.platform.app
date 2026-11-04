package com.pl.platform.notification

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

private const val SERVICE = "services/notification-service"
private const val DOMAIN_MAIN = "$SERVICE/notification-domain/src/main"
private const val WEB_MAIN = "$SERVICE/notification-web/src/main"
private const val BASE = "com.pl.platform.notification"
private const val APPLICATION = "$BASE.application"

/** Packages under application/ that are not actions. */
private val NON_ACTION_PACKAGES = setOf("port", "shared")

private val FORBIDDEN_IN_DOMAIN =
    listOf(
        "org.jboss.resteasy.reactive.server.",
        "io.quarkus.resteasy.",
        "org.eclipse.microprofile.openapi.",
        "$BASE.web.",
    )

/** JAX-RS annotations are fine in infrastructure (the REST client of delivery-service), not in domain/application. */
private val FORBIDDEN_IN_CORE = listOf("jakarta.ws.rs.", "io.vertx.", "io.agroal.")

private val FORBIDDEN_IN_WEB =
    listOf(
        "$BASE.infrastructure.",
        "io.vertx.",
        "org.eclipse.microprofile.reactive.messaging.",
        "io.quarkus.scheduler.",
    )

class ArchitectureTest {
    @Test
    fun `domain project has no HTTP server or OpenAPI and does not see web`() {
        Konsist.scopeFromDirectory(DOMAIN_MAIN).files.assertFalse { file ->
            file.hasImport { import -> FORBIDDEN_IN_DOMAIN.any { import.name.startsWith(it) } }
        }
    }

    @Test
    fun `domain and application know no HTTP or database API`() {
        listOf("domain", "application").forEach { pkg ->
            Konsist.scopeFromDirectory("$DOMAIN_MAIN/kotlin/${BASE.replace('.', '/')}/$pkg").files.assertFalse { file ->
                file.hasImport { import -> FORBIDDEN_IN_CORE.any { import.name.startsWith(it) } }
            }
        }
    }

    @Test
    fun `web project talks to actions, not to infrastructure`() {
        Konsist.scopeFromDirectory(WEB_MAIN).files.assertFalse { file ->
            file.hasImport { import -> FORBIDDEN_IN_WEB.any { import.name.startsWith(it) } }
        }
    }

    @Test
    fun `an action does not import another action`() {
        // Gradle runs tests with the project dir (notification-web) as the working dir.
        val applicationDir = File("../notification-domain/src/main/kotlin/${APPLICATION.replace('.', '/')}")
        val actions = applicationDir.listFiles { f -> f.isDirectory }.orEmpty().map { it.name } - NON_ACTION_PACKAGES
        check(actions.isNotEmpty()) { "No action packages found in ${applicationDir.absolutePath}" }

        actions.forEach { action ->
            val others = actions - action
            Konsist
                .scopeFromDirectory("$DOMAIN_MAIN/kotlin/${APPLICATION.replace('.', '/')}/$action")
                .files
                .assertFalse { file ->
                    file.hasImport { import -> others.any { import.name.startsWith("$APPLICATION.$it.") } }
                }
        }
    }

    @Test
    fun `handlers live in action packages`() {
        Konsist
            .scopeFromDirectory(DOMAIN_MAIN)
            .classes()
            .filter { it.name.endsWith("Handler") }
            .assertTrue { handler ->
                val pkg = handler.packagee?.name.orEmpty()
                pkg.startsWith("$APPLICATION.") && pkg.removePrefix("$APPLICATION.") !in NON_ACTION_PACKAGES
            }
    }
}
