package com.hendraanggrian.generating.r

import com.helger.css.ECSSVersion
import com.hendraanggrian.generating.RConfigurationDsl
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/** A specification for field generation of CSS file. */
@RConfigurationDsl
interface CssROptions {
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
    var writeElementTypeSelector: Boolean

    /**
     * Determine whether adapter should write class selector.
     * Default is true.
     */
    var writeClassSelector: Boolean

    /**
     * Determine whether adapter should write ID selector.
     * Default is true.
     */
    var writeIdSelector: Boolean
}

internal class CssROptionsImpl : CssROptions {
    override var charset: Charset = StandardCharsets.UTF_8
    override var cssVersion: ECSSVersion = ECSSVersion.CSS30
    override var writeElementTypeSelector: Boolean = false
    override var writeClassSelector: Boolean = true
    override var writeIdSelector: Boolean = true
}
