package com.hendraanggrian.generating.buildconfig

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

open class BuildConfigTask : DefaultTask() {

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

    @OutputDirectory lateinit var outputDirectory: File

    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        logger.log(LogLevel.INFO, "Deleting old $className")
        outputDirectory.deleteRecursively()

        logger.log(LogLevel.INFO, "Preparing new $className")
        outputDirectory.mkdirs()

        logger.log(LogLevel.INFO, "Writing new $className")
        JavaFile.builder(packageName, TypeSpec.classBuilder(className)
            .addModifiers(PUBLIC, FINAL)
            .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build())
            .addField(String::class.java, NAME, appName)
            .addField(String::class.java, GROUP, groupId)
            .addField(String::class.java, VERSION, version)
            .addField(Boolean::class.java, DEBUG, debug)
            .apply {
                if (artifactId.isNotBlank()) addField(String::class.java, ARTIFACT, artifactId)
                if (desc.isNotBlank()) addField(String::class.java, DESC, desc)
                if (email.isNotBlank()) addField(String::class.java, EMAIL, email)
                if (website.isNotBlank()) addField(String::class.java, WEBSITE, website)
                fields.forEach { (type, name, value) -> addField(type, name, value!!) }
            }
            .build())
            .addFileComment("Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            .build()
            .writeTo(outputDirectory)
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

    internal companion object {
        const val NAME = "NAME"
        const val GROUP = "GROUP"
        const val VERSION = "VERSION"
        const val DEBUG = "DEBUG"

        const val ARTIFACT = "ARTIFACT"
        const val DESC = "DESC"
        const val EMAIL = "EMAIL"
        const val WEBSITE = "WEBSITE"

        fun TypeSpec.Builder.addField(type: Class<*>, name: String, value: Any): TypeSpec.Builder = addField(
            FieldSpec.builder(type, name, PUBLIC, STATIC, FINAL)
                .initializer(
                    when (type) {
                        String::class.java -> "\$S"
                        Char::class.java -> "'\$L'"
                        else -> "\$L"
                    }, value
                )
                .build()
        )
    }
}