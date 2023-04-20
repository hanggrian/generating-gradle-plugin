package com.hendraanggrian.generating

import com.hendraanggrian.generating.internal.AbstractGenerateTask
import com.hendraanggrian.javapoet.FINAL
import com.hendraanggrian.javapoet.PRIVATE
import com.hendraanggrian.javapoet.PUBLIC
import com.hendraanggrian.javapoet.STATIC
import com.hendraanggrian.javapoet.buildFieldSpec
import com.hendraanggrian.javapoet.buildJavaFile
import com.squareup.javapoet.FieldSpec
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern
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
     * Application name in `BuildConfig.NAME` field. Default value is `application.applicationName`
     * when the plugin is applied, otherwise it is project name.
     */
    @Input
    val applicationName: Property<String> = project.objects.property()

    /** Application version in `BuildConfig.VERSION` field. Default value is project version. */
    @Input
    val applicationVersion: Property<String> = project.objects.property()

    /** Group ID in `BuildConfig.GROUP` field. Default value is project group. */
    @Input
    val groupId: Property<String> = project.objects.property()

    private val buildConfigFields = mutableSetOf<FieldSpec>()

    @TaskAction
    fun generate() {
        logger.info("Generating BuildConfig:")
        require(packageName.get().isNotBlank()) { "Package name cannot be empty." }
        require(className.get().isNotBlank()) { "Class name cannot be empty." }

        val outputDir = outputDirectory.asFile.get()
        outputDir.mkdirs()
        outputDir.resolve(className.get()).takeIf { it.exists() }?.delete()

        addField(String::class, "NAME", applicationName.get())
        addField(String::class, "VERSION", applicationVersion.get())
        addField(String::class, "GROUP", groupId.get())

        buildJavaFile(packageName.get()) {
            comment = "Generated at " + now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))
            addClass(className.get()) {
                addModifiers(PUBLIC, FINAL)
                methods.addConstructor { addModifiers(PRIVATE) }
                fields.addAll(buildConfigFields)
            }
        }.writeTo(outputDir)
        logger.info("- Generated")
    }

    /**
     * Add custom field specifying its type, name, and value.
     *
     * @param type Java class of value.
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     */
    fun <T> addField(type: Class<T>, name: String, value: T) {
        logger.info("- $name = $value")
        buildConfigFields += buildFieldSpec(type, name, PUBLIC, STATIC, FINAL) {
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
}
