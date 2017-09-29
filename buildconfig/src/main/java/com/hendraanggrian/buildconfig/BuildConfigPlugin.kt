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
import javax.lang.model.element.Modifier

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class BuildConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val ext = project.extensions.create("buildconfig", BuildConfigExtension::class.java)
        project.afterEvaluate {
            // requirement checks
            require(ext.packageName.isNotBlank(), { "Package name must not be blank." })
            require(ext.className.isNotBlank(), { "Class name must not be blank." })

            val map = LinkedHashMap<String, String>()
            map.put("GROUP", ext.groupId ?: "unspecified")
            map.put("ARTIFACT", ext.artifactId ?: "unspecified")
            map.put("VERSION", ext.version ?: "unspecified")

            // class generation
            val outputDir = project.projectDir.resolve(ext.pathToJava)
            project.tasks.create("buildconfig").apply {
                outputs.dir(outputDir)
                doLast {
                    Files.deleteIfExists(Paths.get(outputDir.absolutePath, *ext.packageName.split('.').toTypedArray(), ext.className))
                    generateClass(map, outputDir, ext.packageName, ext.className)
                }
            }
        }
    }

    companion object {
        private fun generateClass(map: Map<String, String>, outputDir: File, packageName: String, className: String) = JavaFile
                .builder(packageName, TypeSpec.classBuilder(className)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())
                        .apply {
                            map.keys.forEach {
                                addField(FieldSpec.builder(String::class.java, it, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                                        .initializer("\$S", map[it])
                                        .build())
                            }
                        }
                        .build())
                .addFileComment("buildconfig generated this class at ${LocalDateTime.now()}.")
                .build()
                .writeTo(outputDir)
    }
}