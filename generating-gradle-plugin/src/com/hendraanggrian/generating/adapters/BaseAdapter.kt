package com.hendraanggrian.generating.adapters

import com.hendraanggrian.generating.isJavaName
import com.hendraanggrian.generating.toJavaNameOrNull
import com.hendraanggrian.javapoet.TypeSpecBuilder
import org.gradle.api.logging.Logger
import java.io.File
import javax.lang.model.element.Modifier

/** Where the R fields writing process starts, implementation of each adapter may differ. */
internal abstract class BaseAdapter(
    private val isUppercaseField: Boolean,
    val logger: Logger
) {

    abstract fun process(typeBuilder: TypeSpecBuilder, file: File): Boolean

    protected fun TypeSpecBuilder.addField(name: String): Unit = addField(name, name)

    protected fun TypeSpecBuilder.addField(name: String, value: String) {
        var fieldName: String? = name
        if (isUppercaseField) {
            fieldName = fieldName!!.toUpperCase()
        }
        if (!fieldName!!.isJavaName()) {
            fieldName = fieldName.toJavaNameOrNull()
        }
        // checks if field name is valid and there's no duplicate
        if (fieldName != null && fieldName !in build().fieldSpecs.map { it.name }) {
            fields.add<String>(fieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL) {
                initializer("%S", value)
            }
        }
    }
}
