package com.hendraanggrian.buildconfig

import com.hendraanggrian.buildconfig.BuildConfigPlugin.Companion.EXTENSION_NAME
import com.squareup.javapoet.FieldSpec.builder
import com.squareup.javapoet.JavaFile.builder
import com.squareup.javapoet.MethodSpec.constructorBuilder
import com.squareup.javapoet.TypeSpec.classBuilder
import java.io.File
import java.io.Serializable
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

class BuildConfigWriter internal constructor(
    private val packageName: String,
    private val className: String,
    private val fields: Map<String, Pair<Class<*>, Any>>
) : Serializable {

    internal fun write(outputDirectory: File) = builder(packageName, classBuilder(className)
        .addModifiers(PUBLIC, FINAL)
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .apply {
            fields.keys.forEach { name ->
                val (type, value) = fields[name]!!
                addField(builder(type, name, PUBLIC, STATIC, FINAL)
                    .initializer(when (type) {
                        String::class.java -> "\$S"
                        Char::class.java -> "'\$L'"
                        else -> "\$L"
                    }, value)
                    .build())
            }
        }
        .build())
        .addFileComment("$EXTENSION_NAME generated this class at ${now().format(ofPattern("MM-dd-yyyy 'at' h.mm.ss a"))}")
        .build()
        .writeTo(outputDirectory)

    companion object {
        internal const val FIELD_NAME = "NAME"
        internal const val FIELD_GROUP = "GROUP"
        internal const val FIELD_VERSION = "VERSION"
        internal const val FIELD_DEBUG = "DEBUG"
    }
}