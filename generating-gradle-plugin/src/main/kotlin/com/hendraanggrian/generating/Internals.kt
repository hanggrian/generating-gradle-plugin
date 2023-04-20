package com.hendraanggrian.generating

import javax.lang.model.SourceVersion

/** Check if string is a valid Java field name. */
internal fun String.isJavaName(): Boolean = when {
    // Java SE 9 no longer supports '_'
    isEmpty() || this == "_" || !SourceVersion.isName(this) -> false
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
