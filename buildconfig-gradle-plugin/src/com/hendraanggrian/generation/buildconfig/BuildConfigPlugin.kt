package com.hendraanggrian.generation.buildconfig

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.ide.idea.model.IdeaModel

class BuildConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.run {
            tasks {
                val generateBuildConfig = register("generate$CLASS_NAME", BuildConfigTask::class) {
                    group = GROUP_NAME
                }
                afterEvaluate {
                    generateBuildConfig.get().let { task ->
                        if (task.packageName == null) task.packageName = project.group.toString()
                        if (task.appName == null) task.appName = project.name
                        if (task.groupId == null) task.groupId = project.group.toString()
                        if (task.isBuildVersionNull()) task.setBuildVersion(project.version.toString())
                        if (task.debug == null) task.debug = false
                    }
                }

                val compileBuildConfig = register("compile$CLASS_NAME", JavaCompile::class) {
                    dependsOn(generateBuildConfig.get())
                    group = GROUP_NAME
                    classpath = files()
                    destinationDir = buildDir.resolve("$GENERATED_DIRECTORY/buildconfig/classes/main")
                    source(generateBuildConfig.get().outputDir)
                }

                val compiledClasses = files(compileBuildConfig.get().outputs.files.filter {
                    !it.name.endsWith("dependency-cache")
                })
                compiledClasses.builtBy(compileBuildConfig)

                val sourceSet = convention.getPlugin(JavaPluginConvention::class.java).sourceSets["main"]
                sourceSet.compileClasspath += compiledClasses
                compiledClasses.forEach { sourceSet.output.dir(it) }

                require(plugins.hasPlugin("org.gradle.idea")) { "plugin 'idea' must be applied" }
                val providedConfig = configurations.register("provided$CLASS_NAME")
                providedConfig.get().dependencies += dependencies.create(compiledClasses)
                (extensions["idea"] as IdeaModel).module.scopes["PROVIDED"]!!["plus"]!! += providedConfig.get()
            }
        }
    }

    internal companion object {
        const val CLASS_NAME = "BuildConfig"
        const val GROUP_NAME = "generation"
        const val GENERATED_DIRECTORY = "generated"
    }
}