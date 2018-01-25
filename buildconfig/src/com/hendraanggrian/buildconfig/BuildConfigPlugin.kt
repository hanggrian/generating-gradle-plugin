package com.hendraanggrian.buildconfig

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import javax.lang.model.element.Modifier.*

class BuildConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) = project.extensions.create("buildconfig", BuildConfigExtension::class.java).let { ext ->
        project.afterEvaluate {
            project.task("buildconfig").apply {
                val outputDir = project.projectDir.resolve(ext.srcDir)
                doFirst {
                    require(ext.packageName.isNotBlank(), { "Package name must not be blank." })
                    require(ext.className.isNotBlank(), { "Class name must not be blank." })
                    Files.deleteIfExists(Paths.get(outputDir.absolutePath, *ext.packageName.split('.').toTypedArray(), ext.className))
                }
                doLast {
                    generateClass(ext.fields, outputDir, ext.packageName, ext.className)
                }
            }
        }
    }

    companion object {
        private fun generateClass(map: Map<String, Pair<Class<*>, Any>>, outputDir: File, packageName: String, className: String) = JavaFile
                .builder(packageName, TypeSpec.classBuilder(className)
                        .addModifiers(PUBLIC, FINAL)
                        .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build())
                        .apply {
                            map.keys.forEach { name ->
                                val (type, value) = map[name]!!
                                addField(FieldSpec.builder(type, name, PUBLIC, STATIC, FINAL)
                                        .initializer(if (type == String::class.java) "\$S" else "\$L", value)
                                        .build())
                            }
                        }
                        .build())
                .addFileComment("buildconfig generated this class at ${LocalDateTime.now()}.")
                .build()
                .writeTo(outputDir)
    }
}