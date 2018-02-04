package com.hendraanggrian.buildconfig

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.FieldSpec.builder
import javax.lang.model.element.Modifier.*

data class BuildConfigField<out T>(
        private val type: Class<T>,
        val name: String,
        val value: T
) : java.io.Serializable {

    override fun equals(other: Any?): Boolean = other != null && other is BuildConfigField<*> && name == other.name

    override fun hashCode(): Int = name.hashCode()

    fun toFieldSpec(): FieldSpec = builder(type, name, PUBLIC, STATIC, FINAL)
            .initializer(when (type) {
                String::class.java -> "\$S"
                Char::class.java -> "'\$L'"
                else -> "\$L"
            }, value)
            .build()

    companion object {
        internal const val NAME_NAME = "NAME"
        internal const val NAME_GROUP = "GROUP"
        internal const val NAME_VERSION = "VERSION"
        internal const val NAME_DEBUG = "DEBUG"
    }
}