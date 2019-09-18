package com.hendraanggrian.buildconfig

import java.io.Serializable
import javax.lang.model.SourceVersion

internal class BuildConfigField<T>(
    private val type: Class<T>,
    private val name: String,
    private val value: T
) : Serializable {

    init {
        check(SourceVersion.isName(name)) { "$name is not a valid java variable name" }
    }

    override fun hashCode(): Int = name.hashCode()

    override fun equals(other: Any?): Boolean = other != null && other is BuildConfigField<*> && other.name == name

    operator fun component1(): Class<T> = type

    operator fun component2(): String = name

    operator fun component3(): T = value
}