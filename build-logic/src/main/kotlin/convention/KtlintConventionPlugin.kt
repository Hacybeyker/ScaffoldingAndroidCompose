package convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask

class KtlintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(pluginId("ktlint"))

            extensions.configure<KtlintExtension> {
                android.set(true)
                ignoreFailures.set(false)
                reporters {
                    reporter(ReporterType.PLAIN)
                    reporter(ReporterType.HTML)
                    reporter(ReporterType.CHECKSTYLE)
                }
            }

            registerQualityAggregates()

            // El orden se declara sobre cada check por source set (ktlintMainSourceSetCheck, ...): un mustRunAfter
            // sobre el agregado ktlintCheck solo lo ordena a él, y sus subtareas arrancan antes que el formato.
            tasks.withType<KtLintCheckTask>().configureEach { mustRunAfter("ktlintFormat") }
        }
    }
}
