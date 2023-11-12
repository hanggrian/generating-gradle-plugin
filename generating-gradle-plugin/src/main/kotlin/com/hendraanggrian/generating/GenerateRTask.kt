package com.hendraanggrian.generating

import com.hendraanggrian.generating.internal.GenerateTask
import com.hendraanggrian.generating.r.CssAdapter
import com.hendraanggrian.generating.r.CssROptions
import com.hendraanggrian.generating.r.CssROptionsImpl
import com.hendraanggrian.generating.r.JsonAdapter
import com.hendraanggrian.generating.r.JsonROptions
import com.hendraanggrian.generating.r.JsonROptionsImpl
import com.hendraanggrian.generating.r.PathRAdapter
import com.hendraanggrian.generating.r.PropertiesAdapter
import com.hendraanggrian.generating.r.PropertiesROptions
import com.hendraanggrian.generating.r.PropertiesROptionsImpl
import com.hendraanggrian.generating.r.RAdapter
import com.hendraanggrian.javapoet.FINAL
import com.hendraanggrian.javapoet.PRIVATE
import com.hendraanggrian.javapoet.PUBLIC
import com.hendraanggrian.javapoet.STATIC
import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.javapoet.buildJavaFile
import com.hendraanggrian.javapoet.classType
import com.hendraanggrian.javapoet.constructorMethod
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import java.io.File
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern

/**
 * Task to run when `generateR` command is executed. Running this task alone will not bring
 * generated class to current classpath. To do so, run `compileR`, which also depends on this task.
 */
@RConfigurationDsl
open class GenerateRTask : GenerateTask() {
    /** Generated class name, cannot be empty. Default value is `R`. */
    @Input
    val className: Property<String> =
        project.objects.property<String>().convention("R")

    /** When activated, automatically make all field names uppercase. Default value is false. */
    @Input
    val shouldUppercaseField: Property<Boolean> =
        project.objects.property<Boolean>().convention(false)

    /** When activated, automatically make all class names lowercase. Default value is false. */
    @Input
    val shouldLowercaseClass: Property<Boolean> =
        project.objects.property<Boolean>().convention(false)

    /** Main resource directory. Default is last configured sources set resources' dir. */
    @Optional
    @InputDirectory
    val resourcesDirectory: Property<File> = project.objects.property()

    /**
     * Collection of files (or directories) that are ignored from this task. Default value is empty.
     */
    @InputFiles
    val exclusions: SetProperty<File> =
        project.objects.setProperty<File>().convention(emptySet())

    private var cssOptions: CssROptions? = null
    private var propertiesOptions: PropertiesROptions? = null
    private var jsonOptions: JsonROptions? = null

    /** Enable CSS files support with default configuration. */
    fun css() {
        cssOptions = CssROptionsImpl()
    }

    /** Enable CSS files support with customized [action]. */
    fun css(action: Action<in CssROptions>) {
        val options = CssROptionsImpl()
        action(options)
        cssOptions = options
    }

    /** Enable properties files support with default configuration. */
    fun properties() {
        propertiesOptions = PropertiesROptionsImpl()
    }

    /** Enable properties files support with customized [action]. */
    fun properties(action: Action<in PropertiesROptions>) {
        val options = PropertiesROptionsImpl()
        action(options)
        propertiesOptions = options
    }

    /** Enable json files support with default configuration. */
    fun json() {
        jsonOptions = JsonROptionsImpl()
    }

    /** Enable json files support with customized [action]. */
    fun json(action: Action<in JsonROptions>) {
        val options = JsonROptionsImpl()
        action(options)
        jsonOptions = options
    }

    /** Generate R class given provided options. */
    @TaskAction
    fun generate() {
        if (!resourcesDirectory.isPresent || !resourcesDirectory.get().exists()) {
            logger.info("Resources folder doesn't exist.")
            return
        }

        logger.info("Generating R:")
        val resourcesDir = resourcesDirectory.get()
        require(packageName.get().isNotBlank()) { "Package name cannot be empty." }
        require(className.get().isNotBlank()) { "Class name cannot be empty." }

        val outputDir = outputDirectory.asFile.get()
        outputDir.mkdirs()
        outputDir.resolve(className.get()).takeIf { it.exists() }?.delete()

        buildJavaFile(packageName.get()) {
            comment("Generated at " + now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a")))
            var fileName = className.get()
            if (shouldLowercaseClass.get()) {
                fileName = fileName.lowercase()
            }
            classType(fileName) {
                modifiers(PUBLIC, FINAL)
                constructorMethod { modifiers(PRIVATE) }
                processDir(
                    listOfNotNull(
                        cssOptions?.let { CssAdapter(it, shouldUppercaseField.get(), logger) },
                        jsonOptions?.let { JsonAdapter(it, shouldUppercaseField.get(), logger) },
                        propertiesOptions?.let {
                            PropertiesAdapter(
                                it,
                                shouldLowercaseClass.get(),
                                shouldUppercaseField.get(),
                                logger,
                            )
                        },
                    ),
                    PathRAdapter(resourcesDir.path, shouldUppercaseField.get(), logger),
                    resourcesDir,
                )
            }
        }.writeTo(outputDir)
        logger.info("- Generated")
    }

    private fun TypeSpecBuilder.processDir(
        adapters: Iterable<RAdapter>,
        pathRAdapter: PathRAdapter,
        resourcesDir: File,
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
                                innerClassName = innerClassName.lowercase()
                            }
                            classType(innerClassName) {
                                modifiers(PUBLIC, STATIC, FINAL)
                                constructorMethod { modifiers(PRIVATE) }
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
