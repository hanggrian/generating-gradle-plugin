package com.hendraanggrian.buildconfig

import java.io.Serializable
import javax.lang.model.SourceVersion

/** Represents a single field within `BuildConfig` class. */
internal data class BuildConfigField<T>(val type: Class<T>, val name: String, val value: T) : Serializable {

    /** Non-custom field names. */
    companion object {
        // mandatory
        const val NAME = "NAME"
        const val GROUP = "GROUP"
        const val VERSION = "VERSION"
        const val DEBUG = "DEBUG"
        // optional
        const val ARTIFACT = "ARTIFACT"
        const val DESC = "DESC"
        const val EMAIL = "EMAIL"
        const val WEBSITE = "WEBSITE"
    }

    init {
        check(SourceVersion.isName(name)) { "$name is not a valid java variable name" }
    }

    override fun hashCode(): Int = name.hashCode()
    override fun equals(other: Any?): Boolean = other != null && other is BuildConfigField<*> && other.name == name
}
