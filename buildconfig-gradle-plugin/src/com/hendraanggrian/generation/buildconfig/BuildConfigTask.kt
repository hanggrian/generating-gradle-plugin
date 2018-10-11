package com.hendraanggrian.generation.buildconfig

import com.hendraanggrian.generation.buildconfig.BuildConfigPlugin.Companion.CLASS_NAME
import com.squareup.javapoet.FieldSpec.builder
import com.squareup.javapoet.JavaFile.builder
import com.squareup.javapoet.MethodSpec.constructorBuilder
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeSpec.classBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import javax.lang.model.SourceVersion.isName
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
     * Customize `BuildConfig.AUTHOR` value.
     * There is no default.
     */
    @Input var author: String = ""

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

    @Input val fields: MutableMap<String, Pair<Class<*>, Any>> = mutableMapOf()

    @OutputDirectory lateinit var outputDir: File

    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        outputDir.deleteRecursively()
        builder(packageName, classBuilder(CLASS_NAME)
            .addModifiers(PUBLIC, FINAL)
            .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
            .apply {
                add(String::class.java, NAME, appName)
                add(String::class.java, GROUP, groupId)
                add(String::class.java, VERSION, version)
                add(Boolean::class.java, DEBUG, debug)
                if (artifactId.isNotBlank()) add(String::class.java, ARTIFACT, artifactId)
                if (author.isNotBlank()) add(String::class.java, AUTHOR, author)
                if (email.isNotBlank()) add(String::class.java, EMAIL, email)
                if (website.isNotBlank()) add(String::class.java, WEBSITE, website)
                fields.forEach { name, (type, value) -> add(type, name, value) }
            }
            .build())
            .addFileComment("Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            .build()
            .writeTo(outputDir)
    }

    /**
     * Add custom field specifying its type, name, and value.
     * Convenient method for projects using Groovy.
     *
     * @param type java class of value.
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     */
    fun <T : Any> field(type: Class<T>, name: String, value: T) {
        require(isName(name)) { "$name is not a valid java variable name." }
        require(name !in RESERVED_NAMES) { "$name is reserved, use typed functions instead." }
        fields[name] = type to value
    }

    /**
     * Add custom field by only specifying its name, and value.
     * Convenient method for projects using Kotlin DSL Gradle scripts.
     *
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     */
    inline fun <reified T : Any> field(name: String, value: T) = field(T::class.java, name, value)

    private companion object {
        const val NAME = "NAME"
        const val GROUP = "GROUP"
        const val VERSION = "VERSION"
        const val DEBUG = "DEBUG"

        const val ARTIFACT = "ARTIFACT"
        const val AUTHOR = "AUTHOR"
        const val EMAIL = "EMAIL"
        const val WEBSITE = "WEBSITE"

        val RESERVED_NAMES = arrayOf(NAME, GROUP, VERSION, DEBUG)

        fun TypeSpec.Builder.add(type: Class<*>, name: String, value: Any): TypeSpec.Builder =
            addField(
                builder(type, name, PUBLIC, STATIC, FINAL)
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