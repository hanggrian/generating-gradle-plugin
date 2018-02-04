package com.hendraanggrian.buildconfig

import com.hendraanggrian.buildconfig.BuildConfigPlugin.Companion.CLASS_NAME
import com.hendraanggrian.buildconfig.BuildConfigPlugin.Companion.EXTENSION_NAME
import com.squareup.javapoet.JavaFile.builder
import com.squareup.javapoet.MethodSpec.constructorBuilder
import com.squareup.javapoet.TypeSpec.classBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.nio.file.Files.deleteIfExists
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern
import javax.lang.model.element.Modifier.*

open class GenerateBuildConfigTask : DefaultTask() {

    @Input lateinit var packageName: String
    @Input lateinit var fields: Set<BuildConfigField<*>>
    @OutputDirectory lateinit var outputDir: File

    @TaskAction
    @Throws(IOException::class)
    fun generateBuildConfig() {
        deleteIfExists(outputDir.toPath())
        builder(packageName, classBuilder(CLASS_NAME)
                .addModifiers(PUBLIC, FINAL)
                .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
                .apply { fields.forEach { addField(it.toFieldSpec()) } }
                .build())
                .addFileComment("$EXTENSION_NAME generated this class at ${now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
                .build()
                .writeTo(outputDir)
    }
}
