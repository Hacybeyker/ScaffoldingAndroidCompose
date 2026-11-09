package convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            extensions.configure<LibraryExtension> {
                lint { applyLintGates(layout.settingsDirectory.file("lint.xml").asFile, checkDependencies = false) }
                testOptions { unitTests { isIncludeAndroidResources = true } }
            }
            applyAndroidQualityGates()
        }
    }
}
