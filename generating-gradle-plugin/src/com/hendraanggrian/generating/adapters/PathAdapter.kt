package com.hendraanggrian.generating.adapters

import com.hendraanggrian.javapoet.TypeSpecBuilder
import org.gradle.api.logging.Logger
import java.io.File

/**
 * An adapter that writes file paths as field values.
 * When optional features are activated (CSS, properties, etc.), underscore prefix will be applied to field names.
 */
internal class PathAdapter(
    private val resourcesDir: String,
    isUppercaseField: Boolean,
    logger: Logger
) : BaseAdapter(isUppercaseField, logger) {

    var isUnderscorePrefix: Boolean = false

    override fun process(typeBuilder: TypeSpecBuilder, file: File): Boolean {
        typeBuilder.addField(
            buildString {
                if (isUnderscorePrefix) append('_')
                append(file.nameWithoutExtension)
            },
            file.path.substringAfter(resourcesDir).replace('\\', '/')
        )
        return true
    }
}
