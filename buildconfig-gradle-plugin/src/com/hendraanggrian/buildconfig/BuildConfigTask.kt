package com.hendraanggrian.buildconfig

import com.hendraanggrian.javapoet.buildJavaFile
import com.hendraanggrian.javapoet.final
import com.hendraanggrian.javapoet.private
import com.hendraanggrian.javapoet.public
import com.hendraanggrian.javapoet.static
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import kotlin.reflect.KClass
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

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

    /**
     * Directory of which BuildConfig class will be generated to.
     * Default is `build/generated` relative to project directory.
     */
    @OutputDirectory lateinit var outputDir: File

    /** Convenient method to modify output directory with file path. */
    var outputDirectory: String
        @OutputDirectory get() = outputDir.absolutePath
        set(value) {
            outputDir = project.projectDir.resolve(value)
        }

    private val buildConfigFields: MutableSet<BuildConfigField<*>> = mutableSetOf()

    init {
        // always consider this task out of date
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun generate() {
        logger.info("Deleting old $className")
        val outputDir = outputDir
        outputDir.deleteRecursively()

        logger.info("Preparing new $className")
        outputDir.mkdirs()

        logger.info("Writing new $className")
        buildJavaFile(packageName) {
            comment = "Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}"
            addClass(className) {
                addModifiers(public, final)
                methods.addConstructor {
                    addModifiers(private)
                }
                addField(NAME, appName)
                addField(GROUP, groupId)
                addField(VERSION, version)
                addField(DEBUG, debug)
                if (artifactId.isNotBlank()) {
                    addField(ARTIFACT, artifactId)
                }
                if (desc.isNotBlank()) {
                    addField(DESC, desc)
                }
                if (email.isNotBlank()) {
                    addField(EMAIL, email)
                }
                if (website.isNotBlank()) {
                    addField(WEBSITE, website)
                }
                buildConfigFields.forEach { (type, name, value) ->
                    fields.add(type, name) {
                        addModifiers(public, static, final)
                        initializer(
                            when (type) {
                                String::class.java -> "%S"
                                Char::class.java -> "'%L'"
                                else -> "%L"
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
    fun <T> addField(type: Class<T>, name: String, value: T) {
        buildConfigFields += BuildConfigField(type, name, value)
    }

    /**
     * Add custom field specifying its type, name, and value.
     * Convenient method for projects using Groovy.
     *
     * @param type java class of value.
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     */
    fun <T : Any> addField(type: KClass<T>, name: String, value: T) =
        addField(type.java, name, value)

    /**
     * Add custom field by only specifying its name, and value.
     * Convenient method for projects using Kotlin DSL Gradle scripts.
     *
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     */
    inline fun <reified T : Any> addField(name: String, value: T): Unit =
        addField(T::class, name, value)
}
