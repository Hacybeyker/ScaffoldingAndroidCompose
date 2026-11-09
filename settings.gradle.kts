pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ScaffoldingAndroidCompose"
include(":app")

// Un módulo con plugin de Android o de Kotlin sin la convención de calidad ni una exención explícita se
// quedaría sin ktlint, detekt, Lint y Kover, y sin aviso. Falla en lugar de avisar: con la caché de
// configuración, un aviso de la fase de configuración solo se ve la primera vez. Solo mira el proyecto que
// recibe, así que es compatible con Isolated Projects.
// Va aquí y no como plugin de settings en build-logic: ese plugin cargaría el jar de build-logic antes de que el
// build raíz ponga AGP en el classpath, y las convenciones fallarían con NoClassDefFoundError. Los ids van dentro
// del bloque a propósito: la acción se serializa y no puede capturar el script.
gradle.lifecycle.afterProject {
    val applied =
        listOf(
            "com.android.application",
            "com.android.library",
            "com.android.test",
            "com.android.dynamic-feature",
            "com.android.kotlin.multiplatform.library",
            "org.jetbrains.kotlin.android",
            "org.jetbrains.kotlin.jvm",
            "org.jetbrains.kotlin.multiplatform"
        ).filter { id -> pluginManager.hasPlugin(id) }
    val covered =
        listOf("convention.android.application", "convention.android.library", "convention.quality-exempt")
            .any { id -> pluginManager.hasPlugin(id) }
    if (applied.isNotEmpty() && !covered) {
        throw GradleException(
            """
            |El módulo '$path' aplica ${applied.joinToString()} y se quedaría sin los gates de calidad.
            |
            |  - Módulo Android (application o library): usa alias(libs.plugins.convention.android.application)
            |    o alias(libs.plugins.convention.android.library) en lugar de alias(libs.plugins.android.application)
            |    o alias(libs.plugins.android.library).
            |  - Otro tipo de módulo (kotlin("jvm"), com.android.test, Kotlin Multiplatform): la plantilla todavía
            |    no trae gates para él. Para dejarlo sin gates a propósito, declara
            |    alias(libs.plugins.convention.quality.exempt); para tenerlos, escribe su convención en build-logic/.
            """.trimMargin()
        )
    }
}
