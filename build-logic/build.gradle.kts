import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask

plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
}

ktlint {
    android.set(true)
}

tasks.withType<KtLintCheckTask>().configureEach { mustRunAfter("ktlintFormat") }

// compileOnly: las clases de estos plugins las pone el build raíz en el classpath (los declara con
// `apply false`). Con implementation habría dos copias de AGP, la del build raíz y la de esta convención.
dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.kover.gradlePlugin)
    compileOnly(libs.roborazzi.gradlePlugin)
    compileOnly(libs.sonarqube.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "convention.android.application"
            implementationClass = "convention.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "convention.android.library"
            implementationClass = "convention.AndroidLibraryConventionPlugin"
        }
        register("ktlint") {
            id = "convention.ktlint"
            implementationClass = "convention.KtlintConventionPlugin"
        }
        register("qualityExempt") {
            id = "convention.quality-exempt"
            implementationClass = "convention.QualityExemptConventionPlugin"
        }
    }
}
