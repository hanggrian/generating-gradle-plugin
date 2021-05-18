package com.hendraanggrian.generating

import com.helger.css.ECSSVersion
import java.io.Serializable
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.lang.model.SourceVersion

/** Check if string is a valid Java field name. */
internal fun String.isJavaName(): Boolean = when {
    isEmpty() || this == "_" || !SourceVersion.isName(this) -> false // Java SE 9 no longer supports '_'
    else -> first().isJavaIdentifierStart() && drop(1).all { it.isJavaIdentifierPart() }
}

/** Fixes invalid field name, or null if it is un-fixable. */
internal fun String.toJavaNameOrNull(): String? {
    var result = this
    // Return original string if already valid
    if (result.isJavaName()) {
        return result
    }
    // Append underscore if first char is not java identifier start
    if (!result.first().isJavaIdentifierStart()) {
        result = "_$result"
    }
    // Convert all non-java identifier part chars to underscore
    result = result.map { if (it.isJavaIdentifierPart()) it else '_' }.joinToString("")
    // Merge duplicate underscores
    while ("__" in result) {
        result = result.replace("__", "_")
    }
    // Append underscore to keyword and literal
    if (!SourceVersion.isName(result)) {
        result = "_$result"
    }
    // Return successfully fixed string, or null if unfixable
    return when {
        result.isJavaName() -> result
        else -> null
    }
}

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
        const val DESC = "DESC" // Description is reserved by `DefaultTask`
        const val EMAIL = "EMAIL"
        const val URL = "URL"
    }

    init {
        check(SourceVersion.isName(name)) { "$name is not a valid java variable name" }
    }

    override fun hashCode(): Int = name.hashCode()
    override fun equals(other: Any?): Boolean = other != null && other is BuildConfigField<*> && other.name == name
}

internal class DefaultRCssConfiguration : RCssConfiguration {
    override var charset: Charset = StandardCharsets.UTF_8
    override var cssVersion: ECSSVersion = ECSSVersion.CSS30
    override var isWriteElementTypeSelector: Boolean = false
    override var isWriteClassSelector: Boolean = true
    override var isWriteIdSelector: Boolean = true
}

internal class DefaultRJsonConfiguration : RJsonConfiguration {
    override var isRecursive: Boolean = false
    override var isWriteArray: Boolean = true
}

internal class DefaultRPropertiesConfiguration : RPropertiesConfiguration {
    override var isWriteResourceBundle: Boolean = false
}
