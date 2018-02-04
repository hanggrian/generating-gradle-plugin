package com.hendraanggrian.buildconfig

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
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
    @Input lateinit var fields: Map<String, Pair<Class<*>, Any>>
    @OutputDirectory lateinit var outputDir: File

    @TaskAction
    @Throws(IOException::class)
    fun generateBuildConfig() {
        deleteIfExists(outputDir.toPath())
        JavaFile.builder(packageName, TypeSpec.classBuilder("BuildConfig")
                .addModifiers(PUBLIC, FINAL)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build())
                .apply {
                    fields.keys.forEach { name ->
                        val (type, value) = fields[name]!!
                        addField(FieldSpec.builder(type, name, PUBLIC, STATIC, FINAL)
                                .initializer(when (type) {
                                    String::class.java -> "\$S"
                                    Char::class.java -> "'\$L'"
                                    else -> "\$L"
                                }, value)
                                .build())
                    }
                }
                .build())
                .addFileComment("${BuildConfigExtension.name} generated this class at ${now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
                .build()
                .writeTo(outputDir)
    }
}
