@file:Suppress("UnstableApiUsage")

package io.github.hendraanggrian.buildconfig

import com.hendraanggrian.javapoet.buildJavaFile
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass

/** A task that writes `BuildConfig` class based on configuration made within the task. */
open class BuildConfigTask : DefaultTask() {

    /**
     * Package name of which `BuildConfig` class will be generated to, cannot be empty.
     * If left null, project group will be assigned as value.
     */
    @Input
    val packageName: Property<String> = project.objects.property<String>()
        .convention(project.group.toString())

    /**
     * Generated class name, cannot be empty.
     * Default value is `BuildConfig`.
     */
    @Input
    val className: Property<String> = project.objects.property<String>()
        .convention("BuildConfig")

    /**
     * Mandatory field `BuildConfig.NAME` value.
     * If left null, project name will be assigned as value.
     */
    @Input
    val appName: Property<String> = project.objects.property<String>()
        .convention(project.name)

    /**
     * Mandatory field `BuildConfig.GROUP` value.
     * If left null, project group will be assigned as value.
     */
    @Input
    val groupId: Property<String> = project.objects.property<String>()
        .convention(project.group.toString())

    /**
     * Mandatory field `BuildConfig.VERSION` value.
     * If left null, project version will be assigned as value.
     */
    @Input
    val version: Property<String> = project.objects.property<String>()
        .convention(project.version.toString())

    /**
     * Mandatory field `BuildConfig.VERSION` value.
     * Default value is `false`.
     */
    @Input
    val debug: Property<Boolean> = project.objects.property<Boolean>()
        .convention(false)

    /**
     * Optional field `BuildConfig.NAME` value.
     * If left null, field generation will be skipped.
     */
    @Optional
    @Input
    val artifactId: Property<String> = project.objects.property()

    /**
     * Optional field `BuildConfig.DESC` value.
     * If left null, field generation will be skipped.
     */
    @Optional
    @Input
    val desc: Property<String> = project.objects.property()

    /**
     * Optional field `BuildConfig.EMAIL` value.
     * If left null, field generation will be skipped.
     */
    @Optional
    @Input
    val email: Property<String> = project.objects.property()

    /**
     * Optional field `BuildConfig.URL` value.
     * If left null, field generation will be skipped.
     */
    @Optional
    @Input
    val url: Property<String> = project.objects.property()

    private val fields: MutableSet<BuildConfigField<*>> = mutableSetOf()
    private val outputDir: File = project.buildDir.resolve("generated/buildconfig")

    init {
        outputs.upToDateWhen { false } // always consider this task out of date
    }

    @TaskAction
    fun generate() {
        logger.info("Generating BuildConfig:")

        require(packageName.get().isNotBlank()) { "Package name cannot be empty" }
        require(className.get().isNotBlank()) { "Class name cannot be empty" }

        if (outputDir.exists()) {
            logger.info("  Existing source deleted")
            outputDir.deleteRecursively()
        }
        outputDir.mkdirs()

        addField(BuildConfigField.NAME, appName.get())
        addField(BuildConfigField.GROUP, groupId.get())
        addField(BuildConfigField.VERSION, version.get())
        addField(BuildConfigField.DEBUG, debug.get())
        if (artifactId.isPresent) {
            addField(BuildConfigField.ARTIFACT, artifactId.get())
        }
        if (desc.isPresent) {
            addField(BuildConfigField.DESC, desc.get())
        }
        if (email.isPresent) {
            addField(BuildConfigField.EMAIL, email.get())
        }
        if (url.isPresent) {
            addField(BuildConfigField.URL, url.get())
        }
        val javaFile = buildJavaFile(packageName.get()) {
            comment = "Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}"
            addClass(className.get()) {
                addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                methods.addConstructor { addModifiers(Modifier.PRIVATE) }
                this@BuildConfigTask.fields.forEach { (type, name, value) ->
                    fields.add(type, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL) {
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
        }

        javaFile.writeTo(outputSrcDir)
        logger.info("  Source generated")
    }

    internal val outputSrcDir: File @Internal get() = outputDir.resolve("src/main")
    internal val outputClassesDir: File @Internal get() = outputDir.resolve("classes/main")

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
}
