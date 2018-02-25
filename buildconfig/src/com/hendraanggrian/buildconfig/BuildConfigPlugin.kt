package com.hendraanggrian.buildconfig

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.ide.idea.model.IdeaModel

class BuildConfigPlugin : Plugin<Project> {

    private lateinit var project: Project

    override fun apply(target: Project) {
        project = target
        val extension = project.extensions.create(EXTENSION_NAME, BuildConfigExtension::class.java)
        project.afterEvaluate {
            extension.applyDefault(project)

            val generateTask = extension.createGenerateTask()
            val compileTask = generateTask.createCompileTask(extension.getTaskName("compile"))
            val compiledClasses = project.files(compileTask.outputs.files.filter { !it.name.endsWith("dependency-cache") })
            compiledClasses.builtBy(compileTask)

            val sourceSet = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets["main"]
            sourceSet.compileClasspath += compiledClasses
            compiledClasses.forEach { sourceSet.output.dir(it) }

            require(project.plugins.hasPlugin("org.gradle.idea")) { "plugin 'idea' must be applied" }
            val providedConfig = project.configurations.create(extension.getTaskName("provided"))
            providedConfig.dependencies += project.dependencies.create(compiledClasses)
            (project.extensions["idea"] as IdeaModel).module.scopes["PROVIDED"]!!["plus"]!! += providedConfig
        }
    }

    private fun BuildConfigExtension.createGenerateTask(): GenerateBuildConfigTask = project.task(
        mapOf("type" to GenerateBuildConfigTask::class.java),
        getTaskName("generate"),
        closureOf<GenerateBuildConfigTask> {
            group = GROUP_NAME
            writer = toWriter()
            outputDirectory = project.buildDir.resolve("$GENERATED_DIRECTORY/$EXTENSION_NAME/src/main")
        }) as GenerateBuildConfigTask

    private fun GenerateBuildConfigTask.createCompileTask(taskName: String): JavaCompile = project.task(
        mapOf("type" to JavaCompile::class.java, "dependsOn" to this),
        taskName,
        closureOf<JavaCompile> {
            group = GROUP_NAME
            classpath = project.files()
            destinationDir = project.buildDir.resolve("$GENERATED_DIRECTORY/$EXTENSION_NAME/classes/main")
            source(this@createCompileTask.outputDirectory)
        }) as JavaCompile

    companion object {
        internal const val EXTENSION_NAME = "buildconfig"
        private const val GROUP_NAME = "generation"
        private const val GENERATED_DIRECTORY = "generated"
    }
}