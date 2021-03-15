package io.github.hendraanggrian.buildconfig

import com.hendraanggrian.javapoet.buildJavaFile
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
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
    @Input var packageName: String? = null

    /**
     * Generated class name, cannot be empty.
     * Default value is `BuildConfig`.
     */
    @Input var className: String = "BuildConfig"

    /**
     * Mandatory field `BuildConfig.NAME` value.
     * If left null, project name will be assigned as value.
     */
    @Input var appName: String? = null

    /**
     * Mandatory field `BuildConfig.GROUP` value.
     * If left null, project group will be assigned as value.
     */
    @Input var groupId: String? = null

    /**
     * Mandatory field `BuildConfig.VERSION` value.
     * If left null, project version will be assigned as value.
     */
    @Input var version: String? = null

    /**
     * Mandatory field `BuildConfig.VERSION` value.
     * Default value is `false`.
     */
    @Input var debug: Boolean = false

    /**
     * Optional field `BuildConfig.NAME` value.
     * If left null, field generation will be skipped.
     */
    @Optional @Input var artifactId: String? = null

    /**
     * Optional field `BuildConfig.DESC` value.
     * If left null, field generation will be skipped.
     */
    @Optional @Input var desc: String? = null

    /**
     * Optional field `BuildConfig.EMAIL` value.
     * If left null, field generation will be skipped.
     */
    @Optional @Input var email: String? = null

    /**
     * Optional field `BuildConfig.WEBSITE` value.
     * If left null, field generation will be skipped.
     */
    @Optional @Input var website: String? = null

    /**
     * Directory of which `BuildConfig` class will be generated to.
     * Default is `build/generated` relative to project directory.
     */
    @OutputDirectory lateinit var outputDir: File

    /** Convenient method to modify output directory relative to project directory. */
    var outputDirectory: String
        @Input get() = outputDir.absolutePath
        set(value) {
            outputDir = project.projectDir.resolve(value)
        }

    private val fields: MutableSet<BuildConfigField<*>> = mutableSetOf()

    init {
        outputs.upToDateWhen { false } // always consider this task out of date
    }

    @TaskAction fun generate() {
        logger.info("Generating BuildConfig:")

        require(packageName!!.isNotBlank()) { "Package name cannot be empty" }
        require(className.isNotBlank()) { "Class name cannot be empty" }

        if (outputDir.exists()) {
            logger.info("  Existing source deleted")
            outputDir.deleteRecursively()
        }
        outputDir.mkdirs()

        addField(BuildConfigField.NAME, appName!!)
        addField(BuildConfigField.GROUP, groupId!!)
        addField(BuildConfigField.VERSION, version!!)
        addField(BuildConfigField.DEBUG, debug)
        if (artifactId != null) addField(BuildConfigField.ARTIFACT, artifactId!!)
        if (desc != null) addField(BuildConfigField.DESC, desc!!)
        if (email != null) addField(BuildConfigField.EMAIL, email!!)
        if (website != null) addField(BuildConfigField.WEBSITE, website!!)
        val javaFile = buildJavaFile(packageName!!) {
            comment = "Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}"
            addClass(className) {
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

        javaFile.writeTo(outputDir)
        logger.info("  Source generated")
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
}
