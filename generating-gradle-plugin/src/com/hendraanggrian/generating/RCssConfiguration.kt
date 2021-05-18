package com.hendraanggrian.generating

import com.helger.css.ECSSVersion
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/** Settings for customizing the field generation of CSS file. */
interface RCssConfiguration {

    /**
     * Charset to be used in case neither a charset rule nor a BOM is present.
     * Default is [StandardCharsets.UTF_8].
     */
    var charset: Charset

    /**
     * Version to use when parsing CSS file.
     * Default is [ECSSVersion.CSS30].
     */
    var cssVersion: ECSSVersion

    /**
     * Determine whether adapter should write element type selector.
     * Default is false.
     */
    var isWriteElementTypeSelector: Boolean

    /**
     * Determine whether adapter should write class selector.
     * Default is true.
     */
    var isWriteClassSelector: Boolean

    /**
     * Determine whether adapter should write ID selector.
     * Default is true.
     */
    var isWriteIdSelector: Boolean
}
