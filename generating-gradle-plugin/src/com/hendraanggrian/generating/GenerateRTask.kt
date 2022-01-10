package com.hendraanggrian.generating

import com.hendraanggrian.generating.adapters.BaseAdapter
import com.hendraanggrian.generating.adapters.CssAdapter
import com.hendraanggrian.generating.adapters.JsonAdapter
import com.hendraanggrian.generating.adapters.PathAdapter
import com.hendraanggrian.generating.adapters.PropertiesAdapter
import com.hendraanggrian.generating.internal.AbstractGenerateTask
import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.javapoet.buildJavaFile
import org.gradle.api.Action
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
open class GenerateRTask : AbstractGenerateTask() {

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
    val resourcesDirectory: Property<File> = project.objects.property()

    /**
     * Collection of files (or directories) that are ignored from this task.
     * Default is empty.
     */
    @InputFiles
    val exclusions: SetProperty<File> = project.objects.setProperty<File>()
        .convention(emptySet())

    private var cssSpec: Property<CssRSpec> = project.objects.property()
    private var propertiesSpec: Property<PropertiesRSpec> = project.objects.property()
    private var jsonSpec: Property<JsonRSpec> = project.objects.property()
    private val outputDir: File = project.buildDir.resolve("generated/r")

    init {
        outputs.upToDateWhen { false } // always consider this task out of date
    }

    /** Returns [resourcesDirectory] represented as [File]. */
    val resourcesDir: File @InputDirectory get() = resourcesDirectory.get()

    /** Add [exclusions] with vararg arguments, relative to project directory. */
    fun exclude(vararg excludes: String) {
        exclusions.addAll(excludes.map { project.projectDir.resolve(it) })
    }

    /** Enable CSS files support with default configuration. */
    fun enableCss() = cssSpec.set(DefaultCssRSpec(project))

    /** Enable CSS files support with customized [action]. */
    fun css(action: Action<CssRSpec>) {
        enableCss()
        action(cssSpec.get())
    }

    /** Enable properties files support with default configuration. */
    fun enableProperties() = propertiesSpec.set(DefaultPropertiesRSpec(project))

    /** Enable properties files support with customized [action]. */
    fun properties(action: Action<PropertiesRSpec>) {
        enableProperties()
        action(propertiesSpec.get())
    }

    /** Enable json files support with default configuration. */
    fun enableJson() = jsonSpec.set(DefaultJsonRSpec(project))

    /** Enable json files support with customized [action]. */
    fun json(action: Action<JsonRSpec>) {
        enableJson()
        action(jsonSpec.get())
    }

    /** Generate R class given provided options. */
    @TaskAction
    fun generate() {
        if (!enabled.get()) {
            logger.info("R disabled")
            return
        }
        logger.info("Generating R:")

        val resourcesDir = resourcesDirectory.get()
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
                        cssSpec.orNull?.let { CssAdapter(it, shouldUppercaseField.get(), logger) },
                        jsonSpec.orNull?.let { JsonAdapter(it, shouldUppercaseField.get(), logger) },
                        propertiesSpec.orNull?.let {
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
