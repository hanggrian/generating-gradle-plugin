@file:Suppress("UnusedImport")

package com.hendraanggrian.generating.buildconfig

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate // ktlint-disable
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like BuildConfig class with this plugin. */
class BuildConfigPlugin : Plugin<Project> {

    internal companion object {
        const val CLASS_NAME = "BuildConfig"
        const val GROUP_NAME = "generating"
        const val GENERATED_DIRECTORY = "generated"
    }

    override fun apply(project: Project) {
        val generateBuildConfig by project.tasks.registering(BuildConfigTask::class) {
            group = GROUP_NAME
            outputDir = project.buildDir.resolve("$GENERATED_DIRECTORY/buildconfig/src/main")
        }
        project.afterEvaluate {
            generateBuildConfig {
                if (packageName.isEmpty()) packageName = project.group.toString()
                if (appName.isEmpty()) appName = project.name
                if (groupId.isEmpty()) groupId = project.group.toString()
                if (version.isEmpty()) version = project.version.toString()
            }
        }
        val compileBuildConfig by project.tasks.registering(JavaCompile::class) {
            group = GROUP_NAME
            classpath = project.files()
            destinationDir = project.buildDir.resolve("$GENERATED_DIRECTORY/buildconfig/classes/main")

            val generateBuildConfigTask = generateBuildConfig.get()
            dependsOn(generateBuildConfigTask)
            source(generateBuildConfigTask.outputDir)
        }

        val compileBuildConfigTask = compileBuildConfig.get()
        val compiledClasses = project
            .files(compileBuildConfigTask.outputs.files.filter { !it.name.endsWith("dependency-cache") })
        compiledClasses.builtBy(compileBuildConfigTask)
        project.convention.getPlugin<JavaPluginConvention>().sourceSets {
            "main" {
                compileClasspath += compiledClasses
                compiledClasses.forEach { output.dir(it) }
            }
        }

        require(project.plugins.hasPlugin("org.gradle.idea")) { "plugin 'idea' must be applied" }

        project.configurations.register("provided$CLASS_NAME") {
            dependencies += project.dependencies.create(compiledClasses)
            project.extensions
                .getByName<IdeaModel>("idea")
                .module
                .scopes["PROVIDED"]!!["plus"]!! += this
        }
    }
}