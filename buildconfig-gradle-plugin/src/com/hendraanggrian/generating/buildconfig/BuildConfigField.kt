package com.hendraanggrian.generating.buildconfig

import java.io.Serializable
import javax.lang.model.SourceVersion

class BuildConfigField<T>(
    private val type: Class<T>,
    name: String,
    private val value: T
) : Serializable {

    private companion object {
        val RESERVED_NAMES = arrayOf(
            BuildConfigTask.NAME,
            BuildConfigTask.GROUP,
            BuildConfigTask.VERSION,
            BuildConfigTask.DEBUG
        )
    }

    private val name: String

    init {
        require(SourceVersion.isName(name)) { "$name is not a valid java variable name." }
        require(name !in RESERVED_NAMES) { "$name is reserved, use typed functions instead." }
        this.name = name
    }

    override fun hashCode(): Int = name.hashCode()

    override fun equals(other: Any?): Boolean = other != null && other is BuildConfigField<*> && other.name == name

    operator fun component1(): Class<T> = type

    operator fun component2(): String = name

    operator fun component3(): T = value
}