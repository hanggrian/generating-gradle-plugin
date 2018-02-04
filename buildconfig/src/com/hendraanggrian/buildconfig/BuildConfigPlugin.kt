package com.hendraanggrian.buildconfig

import com.hendraanggrian.buildconfig.BuildConfigExtension.Companion.FIELD_GROUP
import com.hendraanggrian.buildconfig.BuildConfigExtension.Companion.FIELD_NAME
import com.hendraanggrian.buildconfig.BuildConfigExtension.Companion.FIELD_VERSION
import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.closureOf
import org.gradle.plugins.ide.idea.model.IdeaModel
import java.io.File

class BuildConfigPlugin : Plugin<Project> {

    private lateinit var project: Project

    override fun apply(p: Project) {
        project = p
        val ext = project.extensions.create(BuildConfigExtension.name, BuildConfigExtension::class.java)
        project.afterEvaluate {
            if (!ext.fields.contains(FIELD_NAME)) ext.name = project.name
            if (!ext.fields.contains(FIELD_GROUP)) ext.group = project.group.findInClosure()
            if (!ext.fields.contains(FIELD_VERSION)) ext.version = project.version.findInClosure()

            val generateTask = ext.generateTask
            generateTask.outputDir = project.buildDir.toPath()
                    .resolve("gen/${BuildConfigExtension.name}/src/main")
                    .toFile()

            val compileTask = generateTask.compileTask
            val compiledClasses = project.files(compileTask.outputs.files.filter { !it.name.endsWith("dependency-cache") })
            compiledClasses.builtBy(compileTask)

            val sourceSet = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName("main")
            sourceSet.compileClasspath += compiledClasses
            compiledClasses.forEach { sourceSet.output.dir(it) }

            require(project.plugins.hasPlugin("org.gradle.idea")) { "plugin 'idea' must be applied" }
            val providedConfig = project.configurations.create("providedBuildConfig")
            providedConfig.dependencies.add(project.dependencies.create(compiledClasses))
            (project.extensions.getByName("idea") as IdeaModel).module.scopes["PROVIDED"]!!["plus"]!! += providedConfig
        }
    }

    private val BuildConfigExtension.generateTask: GenerateBuildConfigTask
        get() = project.task(mapOf("type" to GenerateBuildConfigTask::class.java), "generateBuildConfig", closureOf<GenerateBuildConfigTask> {
            packageName = this@generateTask.packageName
            fields = this@generateTask.fields
        }) as GenerateBuildConfigTask

    private val GenerateBuildConfigTask.compileTask: JavaCompile
        get() = project.task(mapOf("type" to JavaCompile::class.java, "dependsOn" to this), "compileBuildConfig", closureOf<JavaCompile> {
            classpath = project.files()
            destinationDir = File("${project.buildDir}/gen/${BuildConfigExtension.name}/classes/main")
            source(this@compileTask.outputDir)
        }) as JavaCompile

    companion object {
        private fun Any.findInClosure(): String {
            var s = this
            while (s is Closure<*>) s = s.call()
            return when (s) {
                is String -> s
                else -> s.toString()
            }
        }
    }
}