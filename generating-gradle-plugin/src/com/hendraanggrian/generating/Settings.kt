package com.hendraanggrian.generating

import com.helger.css.ECSSVersion
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Settings for customizing the field generation of CSS file.
 * @param charset charset to be used in case neither a charset rule nor a BOM is present, default is [StandardCharsets.UTF_8].
 * @param cssVersion version to use when parsing CSS file, default is [ECSSVersion.CSS30].
 * @param isWriteElementTypeSelector determine whether adapter should write element type selector, default is false.
 * @param isWriteClassSelector determine whether adapter should write class selector, default is true.
 * @param isWriteIdSelector determine whether adapter should write ID selector, default is true.
 * @see RTask.configureCss
 */
data class CssSettings(
    var charset: Charset = StandardCharsets.UTF_8,
    var cssVersion: ECSSVersion = ECSSVersion.CSS30,
    var isWriteElementTypeSelector: Boolean = false,
    var isWriteClassSelector: Boolean = true,
    var isWriteIdSelector: Boolean = true
) {

    /** Groovy-friendly alias of [charset]. */
    fun charset(charset: Charset) {
        this.charset = charset
    }

    /** Groovy-friendly alias of [cssVersion]. */
    fun cssVersion(cssVersion: ECSSVersion) {
        this.cssVersion = cssVersion
    }

    /** Groovy-friendly alias of [isWriteElementTypeSelector]. */
    fun writeElementTypeSelector(writeElementTypeSelector: Boolean) {
        isWriteElementTypeSelector = writeElementTypeSelector
    }

    /** Groovy-friendly alias of [isWriteClassSelector]. */
    fun writeClassSelector(writeClassSelector: Boolean) {
        isWriteClassSelector = writeClassSelector
    }

    /** Groovy-friendly alias of [isWriteIdSelector]. */
    fun writeIdSelector(writeIdSelector: Boolean) {
        isWriteIdSelector = writeIdSelector
    }
}

/**
 * Settings for customizing the field generation of json file.
 * @param isRecursive determine whether adapter should also write inner json object, default is false.
 * @param isWriteArray extended property of [isRecursive] that will also write inner json array, default is true.
 * @see RTask.configureJson
 */
data class JsonSettings(
    var isRecursive: Boolean = false,
    var isWriteArray: Boolean = true
) {

    /** Groovy-friendly alias of [isRecursive]. */
    fun recursive(recursive: Boolean) {
        isRecursive = recursive
    }

    /** Groovy-friendly alias of [isWriteArray]. */
    fun writeArray(writeArray: Boolean) {
        isWriteArray = writeArray
    }
}

/**
 * Settings for customizing the field generation of properties file.
 * @param isWriteResourceBundle determine whether adapter should also write resource bundle, default is false.
 * @see RTask.configureProperties
 */
data class PropertiesSettings(
    var isWriteResourceBundle: Boolean = false
) {

    /** Groovy-friendly alias of [isWriteResourceBundle]. */
    fun writeResourceBundle(writeResourceBundle: Boolean) {
        isWriteResourceBundle = writeResourceBundle
    }
}
