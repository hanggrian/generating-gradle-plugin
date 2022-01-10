package com.hendraanggrian.generating

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like `BuildConfig` class with this plugin. */
class GeneratingPlugin : Plugin<Project> {
    companion object {
        const val GENERATING_GROUP = "generating"
        const val GENERATE_BUILDCONFIG_TASK_NAME = "generateBuildConfig"
        const val GENERATE_R_TASK_NAME = "generateR"
        const val COMPILE_BUILDCONFIG_TASK_NAME = "compileBuildConfig"
        const val COMPILE_R_TASK_NAME = "compileR"
    }

    override fun apply(project: Project) {
        require(project.pluginManager.hasPlugin("java") || project.pluginManager.hasPlugin("java-library")) {
            "Generating Plugin requires `java` or `java-library`."
        }
        project.pluginManager.apply(IdeaPlugin::class)

        val generateBuildConfig = project.tasks.register<GenerateBuildConfigTask>(GENERATE_BUILDCONFIG_TASK_NAME) {
            group = GENERATING_GROUP
            description = "Generate Android-like BuildConfig class."
        }
        val generateR = project.tasks.register<GenerateRTask>(GENERATE_R_TASK_NAME) {
            group = GENERATING_GROUP
            description = "Generate Android-like R class."
        }

        val compileBuildConfig = project.tasks.register<JavaCompile>(COMPILE_BUILDCONFIG_TASK_NAME) {
            dependsOn(generateBuildConfig)
            group = GENERATING_GROUP
            description = "Compiles BuildConfig source file."
            classpath = project.files()
            destinationDirectory.set(generateBuildConfig.get().outputClassesDir)
            source(generateBuildConfig.get().outputSrcDir)
        }
        val compileR = project.tasks.register<JavaCompile>(COMPILE_R_TASK_NAME) {
            dependsOn(generateR)
            description = "Compiles R source file."
            group = GENERATING_GROUP
            classpath = project.files()
            destinationDirectory.set(generateR.get().outputClassesDir)
            source(generateR.get().outputSrcDir)
        }

        project.afterEvaluate {
            generateBuildConfig {
                packageName.convention(project.group.toString())
                appName.convention(project.name)
                appVersion.convention(project.version.toString())
                groupId.convention(project.group.toString())
            }
            generateR {
                packageName.convention(project.group.toString())
                resourcesDirectory.convention(
                    project.extensions.getByName<SourceSetContainer>("sourceSets")["main"].resources.srcDirs.last()
                )
            }
        }

        val compiledClasses = project
            .files(
                compileBuildConfig.get().outputs.files +
                    compileR.get().outputs.files.filter { !it.name.endsWith("dependency-cache") }
            )
            .builtBy(compileBuildConfig, compileR)
        project.extensions.getByType<JavaPluginExtension>().sourceSets {
            "main" {
                compileClasspath += compiledClasses
                compiledClasses.forEach { output.dir(it) }
            }
        }
        val providedGenerating by project.configurations.registering {
            dependencies += project.dependencies.create(compiledClasses)
        }
        project.extensions.getByName<IdeaModel>("idea").module
            .scopes["PROVIDED"]!!["plus"]!! += providedGenerating.get()
    }
}
