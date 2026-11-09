import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import dev.detekt.gradle.extensions.DetektExtension
import io.github.takahirom.roborazzi.RoborazziExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.sonarqube.gradle.SonarExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.sonarqube)
}

val sonarHostUrl =
    providers.environmentVariable("SONAR_HOST_URL").orNull
        ?.takeIf { it.isNotBlank() }
        ?: "https://sonarcloud.io"

sonar {
    properties {
        property("sonar.projectKey", "com.hacybeyker.scaffoldingandroidcompose")
        property("sonar.organization", "{{SONAR_ORG}}")
        property("sonar.projectName", "ScaffoldingAndroidCompose")
        property("sonar.host.url", sonarHostUrl)
        property("sonar.projectVersion", libs.versions.appVersion.get())
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.exclusions", "**/*.webp,**/*.png,**/*.jar")
        property("sonar.qualitygate.wait", "true")
    }
}

// Calidad común a todos los módulos Android. Se aplica sola cuando el módulo declara
// com.android.application o com.android.library: ningún módulo declara ni configura estos gates, así que
// uno nuevo no puede quedarse sin ellos. Lo propio de cada módulo (namespace, dependencias) va en su build.
val qualityPluginIds =
    listOf(libs.plugins.ktlint, libs.plugins.detekt, libs.plugins.kover, libs.plugins.roborazzi)
        .map { it.get().pluginId }

val htmlReports =
    mapOf(
        "Android Lint" to "lint-results-debug.html",
        "detekt" to "detekt/detekt.html",
        "ktlint (main)" to "ktlint/ktlintMainSourceSetCheck/ktlintMainSourceSetCheck.html",
        "ktlint (test)" to "ktlint/ktlintTestSourceSetCheck/ktlintTestSourceSetCheck.html",
        "ktlint (scripts)" to "ktlint/ktlintKotlinScriptCheck/ktlintKotlinScriptCheck.html",
        "Kover (cobertura)" to "kover/htmlDebug/index.html"
    )

fun Lint.applyQualityGates(lintFile: File) {
    abortOnError = true
    warningsAsErrors = false
    checkDependencies = true
    checkReleaseBuilds = true
    lintConfig = lintFile
}

fun Project.applyQualityGates() {
    qualityPluginIds.forEach { pluginManager.apply(it) }

    extensions.configure<KtlintExtension> {
        android.set(true)
        ignoreFailures.set(false)
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.HTML)
            reporter(ReporterType.CHECKSTYLE)
        }
    }

    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        parallel = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        basePath = rootProject.projectDir
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

    val modulePath = path
    rootProject.tasks.named("sonar") {
        dependsOn(
            listOf("lint", "detekt", "ktlintCheck", "testDebugUnitTest", "koverXmlReportDebug")
                .map { "$modulePath:$it" }
        )
    }

    tasks.withType<Test>().configureEach {
        failOnNoDiscoveredTests = false
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

    tasks.register("codeQuality") {
        group = "verification"
        description = "Ejecuta Android Lint + ktlint + detekt en un solo comando."
        dependsOn("ktlintCheck", "detekt", "lint")
    }

    // El orden se declara sobre cada check por source set (ktlintMainSourceSetCheck, ...): un mustRunAfter
    // sobre el agregado ktlintCheck solo lo ordena a él, y sus subtareas arrancan antes que el formato.
    tasks.withType<KtLintCheckTask>().configureEach { mustRunAfter("ktlintFormat") }
    tasks.configureEach {
        if (name == "detekt" || name == "lint") mustRunAfter("ktlintFormat")
    }

    // ktlint, detekt y lint no compilan los tests: sin esta dependencia, un tipo movido pasa el paso 1 del
    // DoD y rompe recién en `test`. Es el mismo trabajo previo que `test` necesita, así que no se paga dos veces.
    tasks.withType<KotlinCompile>().configureEach { mustRunAfter("ktlintFormat") }

    tasks.register("formatAndAnalyze") {
        group = "verification"
        description =
            "Formatea el codigo (ktlintFormat), verifica todo (ktlintCheck + detekt + lint) y compila los tests."
        dependsOn("ktlintFormat", "codeQuality", "assembleDebugUnitTest")
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

subprojects {
    pluginManager.withPlugin("com.android.application") {
        extensions.configure<ApplicationExtension> {
            lint { applyQualityGates(rootProject.file("lint.xml")) }
            testOptions { unitTests { isIncludeAndroidResources = true } }
        }
        applyQualityGates()
    }
    pluginManager.withPlugin("com.android.library") {
        extensions.configure<LibraryExtension> {
            lint { applyQualityGates(rootProject.file("lint.xml")) }
            testOptions { unitTests { isIncludeAndroidResources = true } }
        }
        applyQualityGates()
    }
}
