package com.hendraanggrian.generating

import com.hendraanggrian.generating.internal.GenerateTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * BuildConfig and R Gradle plugin for Java projects.
 *
 * @see <a href="https://github.com/hendraanggrian/generating-gradle-plugin">generating-gradle-plugin</a>
 */
class GeneratingPlugin : Plugin<Project> {
    companion object {
        const val GROUP: String = LifecycleBasePlugin.BUILD_GROUP
        const val TASK_GENERATE_BUILDCONFIG: String = "generateBuildConfig"
        const val TASK_GENERATE_R: String = "generateR"
    }

    override fun apply(project: Project) {
        require(
            project.pluginManager.hasPlugin("java") ||
                project.pluginManager.hasPlugin("java-library"),
        ) { "Generating Plugin requires `java` or `java-library`." }
        val hasApplicationPlugin = project.pluginManager.hasPlugin("application")
        val mainSourceSet = project.extensions.getByName<SourceSetContainer>("sourceSets")["main"]

        val generateBuildConfig =
            project.tasks.register<GenerateBuildConfigTask>(TASK_GENERATE_BUILDCONFIG) {
                group = GROUP
                description = "Generate Android-like BuildConfig class."
                packageName.convention(project.group.toString())
                applicationName.convention(project.name)
                applicationVersion.convention(project.version.toString())
                groupId.convention(project.group.toString())
            }
        val generateR =
            project.tasks.register<GenerateRTask>(TASK_GENERATE_R) {
                group = GROUP
                description = "Generate Android-like R class."
                packageName.convention(project.group.toString())
                resourcesDirectory.convention(
                    mainSourceSet.resources.srcDirs.lastOrNull()?.takeIf { it.exists() },
                )
            }
        addToSourceSet(mainSourceSet, generateBuildConfig.get())
        addToSourceSet(mainSourceSet, generateR.get())

        project.afterEvaluate {
            if (hasApplicationPlugin) {
                val javaApp = project.extensions.getByType<JavaApplication>()
                val alternatePackage = javaApp.mainClass.get().substringBeforeLast('.')
                generateBuildConfig {
                    packageName.convention(alternatePackage)
                    applicationName.convention(javaApp.applicationName)
                    groupId.convention(alternatePackage)
                }
                generateR {
                    packageName.convention(alternatePackage)
                }
            }
        }
    }

    private fun addToSourceSet(sourceSet: SourceSet, generateTask: GenerateTask) {
        if (!generateTask.isEnabled) {
            return
        }
        sourceSet.java.srcDir(generateTask.outputDirectory)
    }
}
