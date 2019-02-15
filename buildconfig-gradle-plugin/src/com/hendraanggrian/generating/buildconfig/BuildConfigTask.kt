package com.hendraanggrian.generating.buildconfig

import com.hendraanggrian.javapoet.buildJavaFile
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import javax.lang.model.element.Modifier

open class BuildConfigTask : DefaultTask() {
    internal companion object {
        const val NAME = "NAME"
        const val GROUP = "GROUP"
        const val VERSION = "VERSION"
        const val DEBUG = "DEBUG"

        const val ARTIFACT = "ARTIFACT"
        const val DESC = "DESC"
        const val EMAIL = "EMAIL"
        const val WEBSITE = "WEBSITE"
    }

    /**
     * Package name of which `buildconfig` class will be generated to.
     * Default is project group.
     */
    @Input var packageName: String = ""

    /**
     * Class name of BuildConfig, may be modified
     */
    @Input var className: String = "BuildConfig"

    /**
     * Customize `BuildConfig.NAME` value.
     * Default is project name.
     */
    @Input var appName: String = ""

    /**
     * Customize `BuildConfig.GROUP` value.
     * Default is project group.
     */
    @Input var groupId: String = ""

    /**
     * Customize `BuildConfig.VERSION` value.
     * Default is project version.
     */
    @Input var version: String = ""

    /**
     * Customize `BuildConfig.DEBUG` value.
     * Default is false.
     */
    @Input var debug: Boolean = false

    /**
     * Customize `BuildConfig.ARTIFACT` value.
     * There is no default.
     */
    @Input var artifactId: String = ""

    /**
     * Customize `BuildConfig.DESC` value.
     * There is no default.
     */
    @Input var desc: String = ""

    /**
     * Customize `BuildConfig.EMAIL` value.
     * There is no default.
     */
    @Input var email: String = ""

    /**
     * Customize `BuildConfig.WEBSITE` value.
     * There is no default.
     */
    @Input var website: String = ""

    @Input val fields: MutableSet<BuildConfigField<*>> = mutableSetOf()

    @Input lateinit var outputDirectory: String

    val outputDir: File @OutputDirectory get() = project.projectDir.resolve(outputDirectory)

    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        logger.log(LogLevel.INFO, "Deleting old $className")
        val outputDir = outputDir
        outputDir.deleteRecursively()

        logger.log(LogLevel.INFO, "Preparing new $className")
        outputDir.mkdirs()

        logger.log(LogLevel.INFO, "Writing new $className")
        buildJavaFile(packageName) {
            comment("Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            type(className) {
                modifiers(Modifier.PUBLIC, Modifier.FINAL)
                constructor {
                    modifiers(Modifier.PRIVATE)
                }
                field(NAME, appName)
                field(GROUP, groupId)
                field(VERSION, version)
                field(DEBUG, debug)
                if (artifactId.isNotBlank()) field(ARTIFACT, artifactId)
                if (desc.isNotBlank()) field(DESC, desc)
                if (email.isNotBlank()) field(EMAIL, email)
                if (website.isNotBlank()) field(WEBSITE, website)
                fields.forEach { (type, name, value) ->
                    field(type, name) {
                        initializer(
                            when (type) {
                                String::class.java -> "\$S"
                                Char::class.java -> "'\$L'"
                                else -> "\$L"
                            }, value!!
                        )
                    }
                }
            }
        }.writeTo(outputDir)
    }

    /**
     * Add custom field specifying its type, name, and value.
     * Convenient method for projects using Groovy.
     *
     * @param type java class of value.
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     */
    fun <T> field(type: Class<T>, name: String, value: T) {
        fields += BuildConfigField(type, name, value)
    }

    /**
     * Add custom field by only specifying its name, and value.
     * Convenient method for projects using Kotlin DSL Gradle scripts.
     *
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     */
    inline fun <reified T> field(name: String, value: T) = field(T::class.java, name, value)
}