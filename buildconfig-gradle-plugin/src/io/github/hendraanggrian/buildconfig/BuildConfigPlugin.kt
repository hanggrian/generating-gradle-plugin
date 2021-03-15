package io.github.hendraanggrian.buildconfig

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like `BuildConfig` class with this plugin. */
class BuildConfigPlugin : Plugin<Project> {

    companion object {
        const val GROUP_NAME = "generating"
        private const val GENERATED_DIR = "generated/buildconfig"
    }

    override fun apply(project: Project) {
        val generateBuildConfig by project.tasks.registering(BuildConfigTask::class) {
            group = GROUP_NAME
            description = "Generate Android-like BuildConfig class."
            outputDirectory = project.buildDir.resolve("$GENERATED_DIR/src/main").absolutePath
        }
        val generateBuildConfigTask by generateBuildConfig

        // project properties will return correct values after evaluated
        project.afterEvaluate {
            generateBuildConfig {
                if (packageName == null) packageName = project.group.toString()
                if (appName == null) appName = project.name
                if (groupId == null) groupId = project.group.toString()
                if (version == null) version = project.version.toString()
            }
        }

        val compileBuildConfig by project.tasks.registering(JavaCompile::class) {
            dependsOn(generateBuildConfigTask)
            group = GROUP_NAME
            description = "Compiles BuildConfig source file."
            classpath = project.files()
            destinationDir = project.buildDir.resolve("$GENERATED_DIR/classes/main")
            source(generateBuildConfigTask.outputDirectory)
        }
        val compileBuildConfigTask by compileBuildConfig
        val compiledClasses = project
            .files(compileBuildConfigTask.outputs.files.filter { !it.name.endsWith("dependency-cache") })
            .builtBy(compileBuildConfigTask)

        project.convention.getPlugin<JavaPluginConvention>().sourceSets {
            "main" {
                compileClasspath += compiledClasses
                compiledClasses.forEach { output.dir(it) }
            }
        }

        require(project.plugins.hasPlugin("org.gradle.idea")) { "Plugin 'idea' must be applied" }

        val providedBuildConfig by project.configurations.registering {
            dependencies += project.dependencies.create(compiledClasses)
        }
        val providedBuildConfigConfig by providedBuildConfig
        project.extensions
            .getByName<IdeaModel>("idea")
            .module
            .scopes["PROVIDED"]!!["plus"]!! += providedBuildConfigConfig
    }
}
