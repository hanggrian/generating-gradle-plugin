package com.hendraanggrian.generating

import com.hendraanggrian.generating.css.CssAdapter
import com.hendraanggrian.generating.css.CssROptions
import com.hendraanggrian.generating.css.CssROptionsImpl
import com.hendraanggrian.generating.internal.AbstractGenerateTask
import com.hendraanggrian.generating.json.JsonAdapter
import com.hendraanggrian.generating.json.JsonROptions
import com.hendraanggrian.generating.json.JsonROptionsImpl
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
import java.util.Optional
import javax.lang.model.element.Modifier

/**
 * Task to run when `generateR` command is executed.
 * Running this task alone will not bring generated class to current classpath.
 * To do so, run `compileR`, which also depends on this task.
 */
@RFileMarker
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

    private var cssOptions: Optional<CssROptions> = Optional.empty()
    private var propertiesOptions: Optional<PropertiesROptions> = Optional.empty()
    private var jsonOptions: Optional<JsonROptions> = Optional.empty()
    private val outputDir: File = project.buildDir.resolve("generated/r")

    /** Enable CSS files support with default configuration. */
    fun css() {
        cssOptions = Optional.of(CssROptionsImpl())
    }

    /** Enable CSS files support with customized [action]. */
    fun css(action: Action<CssROptions>) {
        val options = CssROptionsImpl()
        action(options)
        cssOptions = Optional.of(options)
    }

    /** Enable properties files support with default configuration. */
    fun properties() {
        propertiesOptions = Optional.of(PropertiesROptionsImpl())
    }

    /** Enable properties files support with customized [action]. */
    fun properties(action: Action<PropertiesROptions>) {
        val options = PropertiesROptionsImpl()
        action(options)
        propertiesOptions = Optional.of(options)
    }

    /** Enable json files support with default configuration. */
    fun json() {
        jsonOptions = Optional.of(JsonROptionsImpl())
    }

    /** Enable json files support with customized [action]. */
    fun json(action: Action<JsonROptions>) {
        val options = JsonROptionsImpl()
        action(options)
        jsonOptions = Optional.of(options)
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
        require(packageName.get().isNotBlank()) { "Package name cannot be empty." }
        require(className.get().isNotBlank()) { "Class name cannot be empty." }
        require(resourcesDir.exists() && resourcesDir.isDirectory) { "Resources folder not found." }

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
                        cssOptions.orElse(null)?.let { CssAdapter(it, shouldUppercaseField.get(), logger) },
                        jsonOptions.orElse(null)?.let { JsonAdapter(it, shouldUppercaseField.get(), logger) },
                        propertiesOptions.orElse(null)?.let {
                            PropertiesAdapter(it, shouldLowercaseClass.get(), shouldUppercaseField.get(), logger)
                        }
                    ),
                    PathRAdapter(resourcesDir.path, shouldUppercaseField.get(), logger),
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
        adapters: Iterable<RAdapter>,
        pathRAdapter: PathRAdapter,
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
                                processDir(adapters, pathRAdapter, file)
                            }
                        }
                    }
                    file.isFile -> {
                        pathRAdapter.isUnderscorePrefix = adapters.any { it.process(this, file) }
                        pathRAdapter.process(this, file)
                    }
                }
            }
    }
}
