@file:Suppress("UnusedImport", "unused", "UNUSED_VARIABLE")

package com.hendraanggrian.generating.buildconfig

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate // ktlint-disable
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like BuildConfig class with this plugin. */
class BuildConfigPlugin : Plugin<Project> {

    companion object {
        const val GROUP_NAME = "generating"
    }

    override fun apply(project: Project) {
        val generateBuildConfig by project.tasks.registering(BuildConfigTask::class) {
            group = GROUP_NAME
            outputDirectory = project.buildDir.resolve("generated/buildconfig/src/main")
        }
        val generateBuildConfigTask by generateBuildConfig
        project.afterEvaluate {
            generateBuildConfig {
                if (packageName.isEmpty()) packageName = project.group.toString()
                if (appName.isEmpty()) appName = project.name
                if (groupId.isEmpty()) groupId = project.group.toString()
                if (version.isEmpty()) version = project.version.toString()
            }
        }

        val compileBuildConfig by project.tasks.registering(JavaCompile::class) {
            dependsOn(generateBuildConfigTask)
            group = GROUP_NAME
            classpath = project.files()
            destinationDir = project.buildDir.resolve("generated/buildconfig/classes/main")
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
            project.extensions
                .getByName<IdeaModel>("idea")
                .module
                .scopes["PROVIDED"]!!["plus"]!! += this
        }
    }
}