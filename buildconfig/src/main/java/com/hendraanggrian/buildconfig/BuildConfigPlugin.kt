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

            val map = LinkedHashMap<String, Any>()
            ext.println(ext.packageName + "." + ext.className)

            ext.println("GROUP", ext.groupId)
            map.put("GROUP", ext.groupId)

            ext.println("ARTIFACT", ext.artifactId)
            map.put("ARTIFACT", ext.artifactId)

            ext.println("VERSION", ext.version)
            map.put("VERSION", ext.version)

            ext.println("DEBUG", ext.debug)
            map.put("DEBUG", ext.debug)

            // class generation
            val outputDir = project.projectDir.resolve(ext.srcDir)
            project.tasks.create("buildconfig").apply {
                val oldPath = Paths.get(outputDir.absolutePath, *ext.packageName.split('.').toTypedArray(), ext.className)
                inputs.file(oldPath.toFile())
                doFirst { Files.deleteIfExists(oldPath) }
                outputs.dir(outputDir)
                doLast { generateClass(map, outputDir, ext.packageName, ext.className) }
            }
        }
    }

    private fun BuildConfigExtension.println(message: Any?) {
        if (debug) kotlin.io.println(message)
    }

    private fun BuildConfigExtension.println(message1: Any?, message2: Any?) {
        if (debug) kotlin.io.println("|_$message1 = $message2")
    }

    companion object {
        private fun generateClass(map: Map<String, Any>, outputDir: File, packageName: String, className: String) = JavaFile
                .builder(packageName, TypeSpec.classBuilder(className)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())
                        .apply {
                            map.keys.forEach {
                                addField(FieldSpec.builder(map[it]!!.javaClass, it, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                                        .initializer(if (map[it] is String) "\$S" else "\$L", map[it])
                                        .build())
                            }
                        }
                        .build())
                .addFileComment("buildconfig generated this class at ${LocalDateTime.now()}.")
                .build()
                .writeTo(outputDir)
    }
}