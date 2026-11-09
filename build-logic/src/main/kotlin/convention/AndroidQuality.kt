package convention

import com.android.build.api.dsl.Lint
import dev.detekt.gradle.extensions.DetektExtension
import io.github.takahirom.roborazzi.RoborazziExtension
import java.io.File
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.sonarqube.gradle.SonarExtension

private val htmlReports =
    mapOf(
        "Android Lint" to "lint-results-debug.html",
        "detekt" to "detekt/detekt.html",
        "ktlint (main)" to "ktlint/ktlintMainSourceSetCheck/ktlintMainSourceSetCheck.html",
        "ktlint (test)" to "ktlint/ktlintTestSourceSetCheck/ktlintTestSourceSetCheck.html",
        "ktlint (scripts)" to "ktlint/ktlintKotlinScriptCheck/ktlintKotlinScriptCheck.html",
        "Kover (cobertura)" to "kover/htmlDebug/index.html"
    )

internal fun Lint.applyLintGates(lintFile: File) {
    abortOnError = true
    warningsAsErrors = false
    checkDependencies = true
    checkReleaseBuilds = true
    lintConfig = lintFile
}

internal fun Project.applyAndroidQualityGates() {
    pluginManager.apply(KtlintConventionPlugin::class.java)
    listOf("detekt", "kover", "roborazzi").forEach { pluginManager.apply(pluginId(it)) }

    extensions.configure<DetektExtension> {
        buildUponDefaultConfig.set(true)
        parallel.set(true)
        config.setFrom(layout.settingsDirectory.file("config/detekt/detekt.yml").asFile)
        basePath.set(layout.settingsDirectory)
        // El source por defecto solo cubre src/main y src/test: los unit tests por buildType (src/testDebug) y
        // el código de src/debug o src/release quedaban sin analizar.
        source.setFrom("src")
    }

    extensions.configure<KoverProjectExtension> {
        reports {
            filters {
                includes {
                    classes("*.domain.*", "*.data.*", "*ViewModel*")
                }
                excludes {
                    classes(
                        "*_Impl",
                        "*_Impl$*",
                        "*_Factory",
                        "*_Factory$*",
                        "*Module",
                        "*Module$*",
                        "*Module_*",
                        "*_HiltModules*"
                    )
                }
            }
            verify {
                rule("Line coverage of measured classes (domain, data, ViewModels)") {
                    minBound(90)
                }
            }
        }
    }

    extensions.configure<RoborazziExtension> {
        outputDir.set(file("src/test/screenshots"))
    }

    extensions.configure<SonarExtension> {
        properties {
            property("sonar.androidLint.reportPaths", "build/reports/lint-results-debug.xml")
            property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml")
            property(
                "sonar.kotlin.ktlint.reportPaths",
                listOf(
                    "build/reports/ktlint/ktlintKotlinScriptCheck/ktlintKotlinScriptCheck.xml",
                    "build/reports/ktlint/ktlintMainSourceSetCheck/ktlintMainSourceSetCheck.xml",
                    "build/reports/ktlint/ktlintTestSourceSetCheck/ktlintTestSourceSetCheck.xml"
                ).joinToString(",")
            )
            property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/kover/reportDebug.xml")
            property("sonar.junit.reportPaths", "build/test-results/testDebugUnitTest")
            property(
                "sonar.coverage.exclusions",
                listOf(
                    "**/ui/**",
                    "**/navigation/**",
                    "**/di/**",
                    "**/*Module.kt",
                    "**/MainActivity.kt",
                    "**/MainApplication.kt"
                ).joinToString(",")
            )
        }
    }

    // El `sonar` del raíz depende de la `sonarResolver` de cada proyecto: así el módulo declara lo que Sonar
    // necesita sin tocar el proyecto raíz (lo que Isolated Projects no permite).
    tasks.named("sonarResolver") {
        dependsOn("lint", "detekt", "ktlintCheck", "testDebugUnitTest", "koverXmlReportDebug")
    }

    tasks.withType<Test>().configureEach {
        failOnNoDiscoveredTests.set(false)
        maxHeapSize = "2g"
        // Robolectric 4.17+ en JDK 17+ necesita abrir paquetes internos del JDK
        // (https://robolectric.org/getting-started/). Sin jdk.internal.access, el arranque del
        // entorno de SDK 36+ falla en ApplicationSharedMemory.create().
        jvmArgs(
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED",
            "--add-opens=java.base/java.io=ALL-UNNAMED",
            "--add-opens=java.base/java.net=ALL-UNNAMED",
            "--add-opens=java.base/java.security=ALL-UNNAMED",
            "--add-opens=java.base/java.text=ALL-UNNAMED",
            "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
            "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
            "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED"
        )
    }

    // `detekt` no resuelve tipos, así que reglas como LongParameterList no corren ahí; detektMain y detektTest sí y,
    // juntas, cubren main, debug, release, test y testDebug.
    tasks.named("codeQuality") {
        description = "Ejecuta Android Lint + ktlint + detekt en un solo comando."
        dependsOn("detekt", "detektMain", "detektTest", "lint")
    }

    // El orden se declara sobre cada check por source set (ktlintMainSourceSetCheck, ...): un mustRunAfter
    // sobre el agregado ktlintCheck solo lo ordena a él, y sus subtareas arrancan antes que el formato.
    tasks.configureEach {
        if (name == "detekt" || name == "lint") mustRunAfter("ktlintFormat")
    }

    // ktlint, detekt y lint no compilan los tests: sin esta dependencia, un tipo movido pasa el paso 1 del
    // DoD y rompe recién en `test`. Es el mismo trabajo previo que `test` necesita, así que no se paga dos veces.
    tasks.withType<KotlinCompile>().configureEach { mustRunAfter("ktlintFormat") }

    tasks.named("formatAndAnalyze") {
        description =
            "Formatea el codigo (ktlintFormat), verifica todo (ktlintCheck + detekt + lint) y compila los tests."
        dependsOn("assembleDebugUnitTest")
    }

    val reportsRoot = layout.buildDirectory.dir("reports")
    val reports = htmlReports
    tasks.register("qualityReports") {
        group = "verification"
        description = "Genera los reportes HTML de Lint, detekt, ktlint y Kover. Usa --continue si un gate falla."
        dependsOn("lint", "detekt", "ktlintCheck", "koverHtmlReportDebug")

        doLast {
            logger.lifecycle("\nReportes HTML:")
            reports.forEach { (name, relativePath) ->
                val file = reportsRoot.get().file(relativePath).asFile
                val status = if (file.exists()) file.toURI().toString() else "(no generado)"
                logger.lifecycle("  %-20s %s".format(name, status))
            }
        }
    }
}
