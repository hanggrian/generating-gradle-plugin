package com.hendraanggrian.buildconfig

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec.constructorBuilder
import com.squareup.javapoet.TypeSpec.classBuilder
import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.nio.file.Files.deleteIfExists
import java.nio.file.Paths.get
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.lang.model.element.Modifier.*

class BuildConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create("buildconfig", BuildConfigExtension::class.java)
        project.afterEvaluate {
            //region properties not manually set are updated from project
            if (ext.packageName == null) ext.group = project.group.find()
            if (!ext.fields.contains("NAME")) ext.name = project.name
            if (!ext.fields.contains("VERSION")) ext.version = project.version.find()
            //endregion
            project.task("buildconfig").apply {
                val outputDir = project.buildDir.resolve("ext.srcDir")
                doFirst {
                    deleteIfExists(get(outputDir.absolutePath, *ext.packageName!!.split('.').toTypedArray(), "BuildConfig.java"))
                }
                doLast {
                    JavaFile.builder(ext.packageName, classBuilder("BuildConfig")
                            .addModifiers(PUBLIC, FINAL)
                            .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
                            .apply {
                                ext.fields.keys.forEach { name ->
                                    val (type, value) = ext.fields[name]!!
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
                            .addFileComment("buildconfig generated this class at ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
                            .build()
                            .writeTo(outputDir)
                }
            }
        }
    }

    companion object {
        fun Any.find(): String {
            var s = this
            while (s is Closure<*>) s = s.call()
            return when (s) {
                is String -> s
                else -> s.toString()
            }
        }
    }
}