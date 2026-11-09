package convention

import org.gradle.api.Plugin
import org.gradle.api.Project

// Marca explícita de un módulo sin gates a propósito: el guard de settings.gradle.kts la reconoce y lo deja pasar.
class QualityExemptConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = Unit
}
