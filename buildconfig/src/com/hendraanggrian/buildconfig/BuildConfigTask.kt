package com.hendraanggrian.buildconfig

import com.hendraanggrian.buildconfig.BuildConfigPlugin.Companion.CLASS_NAME
import com.hendraanggrian.buildconfig.BuildConfigPlugin.Companion.GENERATED_DIRECTORY
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
import java.time.LocalDateTime.now
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
    @Input var packageName: String = project.group.toString()

    /**
     * Customize `BuildConfig.APP_NAME` value.
     * Default is project name.
     */
    @Input var appName: String = project.name.toString()

    /**
     * Customize `BuildConfig.GROUP_ID` value.
     * Default is project group.
     */
    @Input var groupId: String = project.group.toString()

    /**
     * Customize `BuildConfig.VERSION` value.
     * Default is project version.
     */
    @Input var version: String = project.version.toString()

    /**
     * Customize `BuildConfig.DEBUG` value.
     * Default is false.
     */
    @Input var debug: Boolean = false

    @Input var fields: MutableMap<String, Pair<Class<*>, Any>> = mutableMapOf()

    @OutputDirectory var outputDir: File = project.buildDir.resolve("$GENERATED_DIRECTORY/buildconfig/src/main")

    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        outputDir.deleteRecursively()
        builder(packageName, classBuilder(CLASS_NAME)
            .addModifiers(PUBLIC, FINAL)
            .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
            .apply {
                add(String::class.java, APP_NAME, appName)
                add(String::class.java, GROUP_ID, groupId)
                add(String::class.java, VERSION, version)
                add(Boolean::class.java, DEBUG, debug)
                fields.forEach { name, (type, value) -> add(type, name, value) }
            }
            .build())
            .addFileComment("Generated at ${now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
            .build()
            .writeTo(outputDir)
    }

    /**
     * Add custom field specifying its name, and value.
     *
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     */
    fun <T : Any> field(type: Class<T>, name: String, value: T) {
        require(isName(name)) { "$name is not a valid java variable name" }
        require(name !in RESERVED_NAMES) { "$name is reserved" }
        fields[name] = type to value
    }

    private fun TypeSpec.Builder.add(type: Class<*>, name: String, value: Any): TypeSpec.Builder = addField(builder(type, name, PUBLIC, STATIC, FINAL)
        .initializer(when (type) {
            String::class.java -> "\$S"
            Char::class.java -> "'\$L'"
            else -> "\$L"
        }, value)
        .build())

    private companion object {
        const val APP_NAME = "APP_NAME"
        const val GROUP_ID = "GROUP_ID"
        const val VERSION = "VERSION"
        const val DEBUG = "DEBUG"
        val RESERVED_NAMES = arrayOf(APP_NAME, GROUP_ID, VERSION, DEBUG)
    }
}
