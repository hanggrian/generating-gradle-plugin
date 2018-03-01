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
        project.run {
            tasks {
                val generateTask = "generate$CLASS_NAME"(BuildConfigTask::class) {
                    group = GROUP_NAME
                    afterEvaluate {
                        if (packageName == null) packageName = project.group.toString()
                        if (appName == null) appName = project.name.toString()
                        if (groupId == null) groupId = project.group.toString()
                        if (isVersionNull()) version(project.version.toString())
                        if (debug == null) debug = false
                    }
                }

                val compileTask = "compile$CLASS_NAME"(JavaCompile::class) {
                    dependsOn(generateTask)
                    group = GROUP_NAME
                    classpath = files()
                    destinationDir = buildDir.resolve("$GENERATED_DIRECTORY/buildconfig/classes/main")
                    source(generateTask.outputDir)
                }

                val compiledClasses = files(compileTask.outputs.files.filter { !it.name.endsWith("dependency-cache") })
                compiledClasses.builtBy(compileTask)

                val sourceSet = convention.getPlugin(JavaPluginConvention::class.java).sourceSets["main"]
                sourceSet.compileClasspath += compiledClasses
                compiledClasses.forEach { sourceSet.output.dir(it) }

                require(plugins.hasPlugin("org.gradle.idea")) { "plugin 'idea' must be applied" }
                val providedConfig = configurations.create("provided$CLASS_NAME")
                providedConfig.dependencies += dependencies.create(compiledClasses)
                (extensions["idea"] as IdeaModel).module.scopes["PROVIDED"]!!["plus"]!! += providedConfig
            }
        }
    }

    internal companion object {
        const val CLASS_NAME = "BuildConfig"
        const val GROUP_NAME = "generation"
        const val GENERATED_DIRECTORY = "generated"
    }
}