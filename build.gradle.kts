// Los plugins con `apply false` ponen sus clases en el classpath que comparten los módulos y build-logic.
// La calidad (ktlint, detekt, Lint, Kover, Roborazzi) vive en build-logic/ como convention plugins.
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
