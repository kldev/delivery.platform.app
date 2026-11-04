package com.pl.platform.delivery

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

private const val SERVICE = "services/delivery-service"
private const val DOMAIN_MAIN = "$SERVICE/delivery-domain/src/main"
private const val WEB_MAIN = "$SERVICE/delivery-web/src/main"
private const val BASE = "com.pl.platform.delivery"
private const val APPLICATION = "$BASE.application"

/** Packages under application/ that are not actions. */
private val NON_ACTION_PACKAGES = setOf("port", "shared")

private val FORBIDDEN_IN_DOMAIN =
    listOf(
        "org.springframework.web.",
        "org.springframework.http.",
        "jakarta.ws.rs.",
        "io.swagger.",
        "org.springdoc.",
        "$BASE.web.",
    )

private val FORBIDDEN_IN_WEB =
    listOf(
        "$BASE.infrastructure.",
        "jakarta.persistence.",
        "org.springframework.data.",
        "org.springframework.kafka.",
    )

class ArchitectureTest {
    @Test
    fun `domain project knows nothing about HTTP or the web project`() {
        Konsist.scopeFromDirectory(DOMAIN_MAIN).files.assertFalse { file ->
            file.hasImport { import -> FORBIDDEN_IN_DOMAIN.any { import.name.startsWith(it) } }
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
        // Gradle runs tests with the project dir (delivery-web) as the working dir.
        val applicationDir = File("../delivery-domain/src/main/kotlin/${APPLICATION.replace('.', '/')}")
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
