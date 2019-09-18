package com.hendraanggrian.buildconfig

import java.io.Serializable
import javax.lang.model.SourceVersion

internal data class BuildConfigField<T>(
    val type: Class<T>,
    val name: String,
    val value: T
) : Serializable {

    init {
        check(SourceVersion.isName(name)) { "$name is not a valid java variable name" }
    }

    override fun hashCode(): Int = name.hashCode()

    override fun equals(other: Any?): Boolean = other != null && other is BuildConfigField<*> && other.name == name
}
