package com.hendraanggrian.buildconfig

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.plugins.ide.idea.model.IdeaModel

class BuildConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks {
            val generateTask = "generate$CLASS_NAME"(BuildConfigTask::class) {
                group = GROUP_NAME
            }

            val compileTask = "compile$CLASS_NAME"(JavaCompile::class) {
                dependsOn(generateTask)
                group = GROUP_NAME
                classpath = project.files()
                destinationDir = project.buildDir.resolve("$GENERATED_DIRECTORY/buildconfig/classes/main")
                source(generateTask.outputDir)
            }

            val compiledClasses = project.files(compileTask.outputs.files.filter { !it.name.endsWith("dependency-cache") })
            compiledClasses.builtBy(compileTask)

            val sourceSet = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets["main"]
            sourceSet.compileClasspath += compiledClasses
            compiledClasses.forEach { sourceSet.output.dir(it) }

            require(project.plugins.hasPlugin("org.gradle.idea")) { "plugin 'idea' must be applied" }
            val providedConfig = project.configurations.create("provided$CLASS_NAME")
            providedConfig.dependencies += project.dependencies.create(compiledClasses)
            (project.extensions["idea"] as IdeaModel).module.scopes["PROVIDED"]!!["plus"]!! += providedConfig
        }
    }

    internal companion object {
        const val CLASS_NAME = "BuildConfig"
        const val GROUP_NAME = "generation"
        const val GENERATED_DIRECTORY = "generated"
    }
}