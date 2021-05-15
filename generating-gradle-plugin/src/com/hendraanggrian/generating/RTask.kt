package com.hendraanggrian.generating

import com.hendraanggrian.generating.adapters.BaseAdapter
import com.hendraanggrian.generating.adapters.CssAdapter
import com.hendraanggrian.generating.adapters.JsonAdapter
import com.hendraanggrian.generating.adapters.PathAdapter
import com.hendraanggrian.generating.adapters.PropertiesAdapter
import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.javapoet.buildJavaFile
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import javax.lang.model.element.Modifier

/** A task that writes `R` class. */
open class RTask : DefaultTask() {

    /**
     * Package name of which `R` class will be generated to, cannot be empty.
     * If left null, project group will be assigned as value.
     */
    @Input
    val packageName: Property<String> = project.objects.property<String>()
        .convention(project.group.toString())

    /**
     * Generated class name, cannot be empty.
     * Default value is `R`.
     */
    @Input
    val className: Property<String> = project.objects.property<String>()
        .convention("R")

    /**
     * When activated, automatically make all field names uppercase.
     * It is disabled by default.
     */
    @Input
    val shouldUppercaseField: Property<Boolean> = project.objects.property<Boolean>()
        .convention(false)

    /**
     * When activated, automatically make all class names lowercase.
     * It is disabled by default.
     */
    @Input
    val shouldLowercaseClass: Property<Boolean> = project.objects.property<Boolean>()
        .convention(false)

    /**
     * Main resources directory.
     * Default is resources folder in main module.
     */
    @InputDirectory
    val resourcesDirectory: DirectoryProperty = project.objects.directoryProperty()

    /**
     * Collection of files (or directories) that are ignored from this task.
     * Default is empty.
     */
    @InputFiles
    val exclusions: SetProperty<File> = project.objects.setProperty<File>()
        .convention(emptySet())

    /** Convenient method to set exclusions relative to project directory. */
    fun exclude(vararg exclusions: String) {
        this.exclusions.set(exclusions.map { project.projectDir.resolve(it) })
    }

    private var cssSettings: CssSettings? = null
    private var propertiesSettings: PropertiesSettings? = null
    private var jsonSettings: JsonSettings? = null
    private val outputDir: File = project.buildDir.resolve("generated${File.separator}r")

    init {
        outputs.upToDateWhen { false } // always consider this task out of date
    }

    /** Enable CSS files support with default configuration. */
    fun configureCss() {
        var settings = cssSettings
        if (settings == null) {
            settings = CssSettings()
            cssSettings = settings
        }
    }

    /** Enable CSS files support with customized [configuration]. */
    fun configureCss(configuration: Action<CssSettings>) {
        configureCss()
        configuration(cssSettings!!)
    }

    /** Enable CSS files support with customized [configuration] in Kotlin DSL. */
    inline fun css(noinline configuration: CssSettings.() -> Unit): Unit =
        configureCss(configuration)

    /** Enable properties files support with default configuration. */
    fun configureProperties() {
        var settings = propertiesSettings
        if (settings == null) {
            settings = PropertiesSettings()
            propertiesSettings = settings
        }
    }

    /** Enable properties files support with customized [configuration]. */
    fun configureProperties(configuration: Action<PropertiesSettings>) {
        configureProperties()
        configuration(propertiesSettings!!)
    }

    /** Enable properties files support with customized [configuration] in Kotlin DSL. */
    inline fun properties(noinline configuration: PropertiesSettings.() -> Unit): Unit =
        configureProperties(configuration)

    /** Enable json files support with default configuration. */
    fun configureJson() {
        var settings = jsonSettings
        if (settings == null) {
            settings = JsonSettings()
            jsonSettings = settings
        }
    }

    /** Enable json files support with customized [configuration]. */
    fun configureJson(configuration: Action<JsonSettings>) {
        configureJson()
        configuration(jsonSettings!!)
    }

    /** Enable json files support with customized [configuration] in Kotlin DSL. */
    inline fun json(noinline configuration: JsonSettings.() -> Unit): Unit =
        configureJson(configuration)

    /** Generate R class given provided options. */
    @TaskAction
    fun generate() {
        logger.info("Generating R:")

        val resourcesDir = resourcesDirectory.get().asFile
        require(packageName.get().isNotBlank()) { "Package name cannot be empty" }
        require(className.get().isNotBlank()) { "Class name cannot be empty" }
        require(resourcesDir.exists() && resourcesDir.isDirectory) { "Resources folder not found" }

        if (outputDir.exists()) {
            logger.info("  Existing source deleted")
            outputDir.deleteRecursively()
        }
        outputDir.mkdirs()

        val javaFile = buildJavaFile(packageName.get()) {
            comment = "Generated at ${LocalDateTime.now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}"
            var fileName = className.get()
            if (shouldLowercaseClass.get()) {
                fileName = fileName.toLowerCase()
            }
            addClass(fileName) {
                addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                methods.addConstructor { addModifiers(Modifier.PRIVATE) }
                processDir(
                    listOfNotNull(
                        cssSettings?.let { CssAdapter(it, shouldUppercaseField.get(), logger) },
                        jsonSettings?.let { JsonAdapter(it, shouldUppercaseField.get(), logger) },
                        propertiesSettings?.let {
                            PropertiesAdapter(it, shouldLowercaseClass.get(), shouldUppercaseField.get(), logger)
                        }
                    ),
                    PathAdapter(resourcesDir.path, shouldUppercaseField.get(), logger),
                    resourcesDir
                )
            }
        }

        javaFile.writeTo(outputSrcDir)
        logger.info("  Source generated")
    }

    internal val outputSrcDir: File @Internal get() = outputDir.resolve("src/main")
    internal val outputClassesDir: File @Internal get() = outputDir.resolve("classes/main")

    private fun TypeSpecBuilder.processDir(
        adapters: Iterable<BaseAdapter>,
        pathAdapter: PathAdapter,
        resourcesDir: File
    ) {
        val exclusionPaths = exclusions.get().map { it.path }
        resourcesDir.listFiles()!!
            .filter { file -> !file.isHidden && file.path !in exclusionPaths }
            .forEach { file ->
                when {
                    file.isDirectory -> {
                        var innerClassName = file.name.toJavaNameOrNull()
                        if (innerClassName != null) {
                            if (shouldLowercaseClass.get()) {
                                innerClassName = innerClassName.toLowerCase()
                            }
                            types.addClass(innerClassName) {
                                addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                                methods.addConstructor { addModifiers(Modifier.PRIVATE) }
                                processDir(adapters, pathAdapter, file)
                            }
                        }
                    }
                    file.isFile -> {
                        pathAdapter.isUnderscorePrefix = adapters.any { it.process(this, file) }
                        pathAdapter.process(this, file)
                    }
                }
            }
    }
}
