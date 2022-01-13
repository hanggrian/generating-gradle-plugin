package com.hendraanggrian.generating

import com.helger.css.ECSSVersion
import org.gradle.api.provider.Property
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/** Settings for customizing the field generation of CSS file. */
@RSpecMarker
interface CssRSpec {

    /**
     * Charset to be used in case neither a charset rule nor a BOM is present.
     * Default is [StandardCharsets.UTF_8].
     */
    var charset: Property<Charset>

    /**
     * Version to use when parsing CSS file.
     * Default is [ECSSVersion.CSS30].
     */
    var cssVersion: Property<ECSSVersion>

    /**
     * Determine whether adapter should write element type selector.
     * Default is false.
     */
    var writeElementTypeSelector: Property<Boolean>

    /**
     * Determine whether adapter should write class selector.
     * Default is true.
     */
    var writeClassSelector: Property<Boolean>

    /**
     * Determine whether adapter should write ID selector.
     * Default is true.
     */
    var writeIdSelector: Property<Boolean>
}
