package convention

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal fun Project.pluginId(alias: String): String =
    extensions.getByType<VersionCatalogsExtension>().named("libs").findPlugin(alias).get().get().pluginId
