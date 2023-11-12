package com.hendraanggrian.generating.r

import com.hendraanggrian.generating.isJavaName
import com.hendraanggrian.generating.toJavaNameOrNull
import com.hendraanggrian.javapoet.FINAL
import com.hendraanggrian.javapoet.PUBLIC
import com.hendraanggrian.javapoet.STATIC
import com.hendraanggrian.javapoet.STRING
import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.javapoet.field
import org.gradle.api.logging.Logger
import java.io.File

/** Where the R fields writing process starts, implementation of each adapter may differ. */
internal abstract class RAdapter(private val isUppercaseField: Boolean, val logger: Logger) {
    abstract fun process(typeBuilder: TypeSpecBuilder, file: File): Boolean

    protected fun TypeSpecBuilder.addField(name: String): Unit = addField(name, name)

    protected fun TypeSpecBuilder.addField(name: String, value: String) {
        var fieldName: String? = name
        if (isUppercaseField) {
            fieldName = fieldName!!.lowercase()
        }
        if (!fieldName!!.isJavaName()) {
            fieldName = fieldName.toJavaNameOrNull()
        }
        // checks if field name is valid and there's no duplicate
        if (fieldName != null && fieldName !in build().fieldSpecs.map { it.name }) {
            field(STRING, fieldName, PUBLIC, STATIC, FINAL) {
                initializer("%S", value)
            }
        }
    }
}

/**
 * An adapter that writes file paths as field values. When optional features are
 * activated (CSS, properties, etc.), underscore prefix will be applied to field names.
 */
internal class PathRAdapter(
    private val resourcesDir: String,
    isUppercaseField: Boolean,
    logger: Logger,
) : RAdapter(isUppercaseField, logger) {
    var isUnderscorePrefix: Boolean = false

    override fun process(typeBuilder: TypeSpecBuilder, file: File): Boolean {
        typeBuilder.addField(
            buildString {
                if (isUnderscorePrefix) append('_')
                append(file.nameWithoutExtension)
            },
            file.path.substringAfter(resourcesDir).replace('\\', '/'),
        )
        return true
    }
}
