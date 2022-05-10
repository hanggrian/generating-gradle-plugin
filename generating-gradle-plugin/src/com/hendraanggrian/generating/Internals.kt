package com.hendraanggrian.generating

import com.helger.css.ECSSVersion
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
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
        const val VERSION = "VERSION"
        const val DEBUG = "DEBUG"
        const val GROUP = "GROUP"

        // optional
        const val EMAIL = "EMAIL"
        const val URL = "URL"
    }

    init {
        check(SourceVersion.isName(name)) { "$name is not a valid java variable name." }
    }

    override fun hashCode(): Int = name.hashCode()
    override fun equals(other: Any?): Boolean = other != null && other is BuildConfigField<*> && other.name == name
}

internal class DefaultCssRSpec(project: Project) : CssRSpec {

    override var charset: Property<Charset> = project.objects.property<Charset>()
        .convention(StandardCharsets.UTF_8)

    override var cssVersion: Property<ECSSVersion> = project.objects.property<ECSSVersion>()
        .convention(ECSSVersion.CSS30)

    override var writeElementTypeSelector: Property<Boolean> = project.objects.property<Boolean>()
        .convention(false)

    override var writeClassSelector: Property<Boolean> = project.objects.property<Boolean>()
        .convention(true)

    override var writeIdSelector: Property<Boolean> = project.objects.property<Boolean>()
        .convention(true)
}

internal class DefaultJsonRSpec(project: Project) : JsonRSpec {

    override var recursive: Property<Boolean> = project.objects.property<Boolean>()
        .convention(false)

    override var writeArray: Property<Boolean> = project.objects.property<Boolean>()
        .convention(true)
}

internal class DefaultPropertiesRSpec(project: Project) : PropertiesRSpec {

    override var writeResourceBundle: Property<Boolean> = project.objects.property<Boolean>()
        .convention(false)
}
