package com.hendraanggrian.generating

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like `BuildConfig` class with this plugin. */
class GeneratingPlugin : Plugin<Project> {
    companion object {
        const val PLUGIN_NAME = "generating"
        const val GROUP_NAME = PLUGIN_NAME
    }

    override fun apply(project: Project) {
        require(project.pluginManager.hasPlugin("java") || project.pluginManager.hasPlugin("java-library")) {
            "Generating Plugin requires `java` or `java-library`."
        }
        project.pluginManager.apply("org.gradle.idea")

        val generateBuildConfig by project.tasks.registering(GenerateBuildConfigTask::class) {
            group = GROUP_NAME
            description = "Generate Android-like BuildConfig class."
        }
        val compileBuildConfig by project.tasks.registering(JavaCompile::class) {
            dependsOn(generateBuildConfig)
            group = GROUP_NAME
            description = "Compiles BuildConfig source file."
            classpath = project.files()
            destinationDir = generateBuildConfig.get().outputClassesDir
            source(generateBuildConfig.get().outputSrcDir)
        }

        val generateR by project.tasks.registering(GenerateRTask::class) {
            group = GROUP_NAME
            description = "Generate Android-like R class."
        }
        val compileR by project.tasks.registering(JavaCompile::class) {
            dependsOn(generateR)
            description = "Compiles R source file."
            group = GROUP_NAME
            classpath = project.files()
            destinationDir = generateR.get().outputClassesDir
            source(generateR.get().outputSrcDir)
        }

        project.afterEvaluate {
            generateBuildConfig {
                packageName.convention(project.group.toString())
                appName.convention(project.name)
                groupId.convention(project.group.toString())
                version.convention(project.version.toString())
            }
            generateR {
                packageName.convention(project.group.toString())
                val sourceSets = project.extensions.getByName<SourceSetContainer>("sourceSets")
                resourcesDirectory.set(sourceSets["main"].resources.srcDirs.last())
            }
        }

        val compiledClasses = project
            .files(
                (compileBuildConfig.get().outputs.files + compileR.get().outputs.files)
                    .filter { !it.name.endsWith("dependency-cache") }
            )
            .builtBy(compileBuildConfig, compileR)
        project.convention.getPlugin<JavaPluginConvention>().sourceSets {
            "main" {
                compileClasspath += compiledClasses
                compiledClasses.forEach { output.dir(it) }
            }
        }
        val providedGenerating by project.configurations.registering {
            dependencies += project.dependencies.create(compiledClasses)
        }
        project.extensions
            .getByName<IdeaModel>("idea")
            .module
            .scopes["PROVIDED"]!!["plus"]!! += providedGenerating.get()
    }
}
