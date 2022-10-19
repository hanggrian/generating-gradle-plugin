package com.hendraanggrian.generating

import com.hendraanggrian.generating.internal.AbstractGenerateTask
import com.hendraanggrian.javapoet.FINAL
import com.hendraanggrian.javapoet.PRIVATE
import com.hendraanggrian.javapoet.PUBLIC
import com.hendraanggrian.javapoet.STATIC
import com.hendraanggrian.javapoet.buildJavaFile
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import javax.lang.model.SourceVersion
import kotlin.reflect.KClass

/**
 * Task to run when `generateBuildConfig` command is executed. Running this task alone will not
 * bring generated class to current classpath. To do so, run `compileBuildConfig`, which also
 * depends on this task.
 */
open class GenerateBuildConfigTask : AbstractGenerateTask() {

    /** Generated class name, cannot be empty. Default value is `BuildConfig`. */
    @Input
    val className: Property<String> = project.objects.property<String>()
        .convention("BuildConfig")

    /**
     * Mandatory field `BuildConfig.NAME` value. If left null, project name will be assigned as
     * value.
     */
    @Input
    val appName: Property<String> = project.objects.property()

    /**
     * Mandatory field `BuildConfig.VERSION` value. If left null, project version will be assigned
     * as value.
     */
    @Input
    val appVersion: Property<String> = project.objects.property()

    /**
     * Mandatory field `BuildConfig.GROUP` value. If left null, project group will be assigned as
     * value.
     */
    @Input
    val groupId: Property<String> = project.objects.property()

    /** Mandatory field `BuildConfig.DEBUG` value. Default value is `false`. */
    @Input
    val debug: Property<Boolean> = project.objects.property<Boolean>()
        .convention(false)

    private val fields: MutableSet<BuildConfigField<*>> = mutableSetOf()

    @TaskAction
    fun generate() {
        logger.info("Generating BuildConfig...")
        require(packageName.get().isNotBlank()) { "Package name cannot be empty." }
        require(className.get().isNotBlank()) { "Class name cannot be empty." }

        val outputDir = outputDirectory.asFile.get()
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        addField(BuildConfigField.NAME, appName.get())
        addField(BuildConfigField.VERSION, appVersion.get())
        addField(BuildConfigField.GROUP, groupId.get())
        addField(BuildConfigField.DEBUG, debug.get())

        buildJavaFile(packageName.get()) {
            comment = "Generated at " +
                LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))
            addClass(className.get()) {
                addModifiers(PUBLIC, FINAL)
                methods.addConstructor { addModifiers(PRIVATE) }
                this@GenerateBuildConfigTask.fields.forEach { (type, name, value) ->
                    fields.add(type, name, PUBLIC, STATIC, FINAL) {
                        initializer(
                            when (type) {
                                String::class.java -> "%S"
                                Char::class.java -> "'%L'"
                                else -> "%L"
                            },
                            value!!
                        )
                    }
                }
            }
        }.writeTo(outputDir)
        logger.info("Source generated.")
    }

    /**
     * Add custom field specifying its type, name, and value.
     *
     * @param type Java class of value.
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     */
    fun <T> addField(type: Class<T>, name: String, value: T) {
        fields += BuildConfigField(type, name, value)
        logger.debug("$name = $value")
    }

    /**
     * Add custom field specifying its Kotlin type, name, and value.
     *
     * @param type Kotlin class of value.
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     * @return `true` if the field has been added, `false` if the field is already exist.
     */
    fun <T : Any> addField(type: KClass<T>, name: String, value: T): Unit =
        addField(type.java, name, value)

    /**
     * Add custom field specifying its reified type, name, and value.
     *
     * @param T reified class of value.
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     * @return `true` if the field has been added, `false` if the field is already exist.
     */
    inline fun <reified T : Any> addField(name: String, value: T): Unit =
        addField(T::class.java, name, value)

    /** Represents a single field within `BuildConfig` class. */
    private data class BuildConfigField<T>(val type: Class<T>, val name: String, val value: T) {

        /** Non-custom field names. */
        companion object {
            // mandatory
            const val NAME = "NAME"
            const val VERSION = "VERSION"
            const val DEBUG = "DEBUG"
            const val GROUP = "GROUP"

            // optional
            const val EMAIL = "EMAIL"
            const val URL = "URL"
        }

        init {
            check(SourceVersion.isName(name)) { "$name is not a valid java variable name." }
        }

        override fun hashCode(): Int = name.hashCode()
        override fun equals(other: Any?): Boolean =
            other != null && other is BuildConfigField<*> && other.name == name
    }
}
