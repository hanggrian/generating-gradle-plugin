package com.hendraanggrian.generation.buildconfig

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.ide.idea.model.IdeaModel

/** Generate Android-like BuildConfig class with this plugin. */
class BuildConfigPlugin : Plugin<Project> {

    internal companion object {
        const val CLASS_NAME = "BuildConfig"
        const val GROUP_NAME = "generation"
        const val GENERATED_DIRECTORY = "generated"
    }

    private lateinit var generateBuildConfig: TaskProvider<BuildConfigTask>
    private lateinit var compileBuildConfig: TaskProvider<JavaCompile>

    override fun apply(project: Project) {
        project.tasks {
            generateBuildConfig = register("generate$CLASS_NAME", BuildConfigTask::class) {
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
            compileBuildConfig = register("compile$CLASS_NAME", JavaCompile::class) {
                dependsOn(generateBuildConfig.get())
                group = GROUP_NAME
                classpath = project.files()
                destinationDir = project.buildDir.resolve("$GENERATED_DIRECTORY/buildconfig/classes/main")
                generateBuildConfig {
                    source(outputDir)
                }
            }
        }

        val compiledClasses = project.files(compileBuildConfig.get().outputs.files.filter {
            !it.name.endsWith("dependency-cache")
        })
        compiledClasses.builtBy(compileBuildConfig.get())
        project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName("main") {
            compileClasspath += compiledClasses
            compiledClasses.forEach { output.dir(it) }
        }

        require(project.plugins.hasPlugin("org.gradle.idea")) { "plugin 'idea' must be applied" }

        project.configurations.register("provided$CLASS_NAME") {
            dependencies += project.dependencies.create(compiledClasses)
            (project.extensions["idea"] as IdeaModel).module.scopes["PROVIDED"]!!["plus"]!! += this
        }
    }
}