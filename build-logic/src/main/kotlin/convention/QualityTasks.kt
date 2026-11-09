package convention

import org.gradle.api.Project

internal fun Project.registerQualityAggregates() {
    tasks.register("codeQuality") {
        group = "verification"
        description = "Verifica el estilo con ktlint."
        dependsOn("ktlintCheck")
    }
    tasks.register("formatAndAnalyze") {
        group = "verification"
        description = "Formatea el codigo con ktlint y lo verifica."
        dependsOn("ktlintFormat", "codeQuality")
    }
}
