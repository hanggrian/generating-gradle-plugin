package com.hendraanggrian.generating.r

import com.hendraanggrian.generating.RConfigurationDsl

/** A specification for field generation of properties file. */
@RConfigurationDsl
interface PropertiesROptions {
    /**
     * Determine whether adapter should also write resource bundle.
     * Default is false.
     */
    var writeResourceBundle: Boolean
}

internal class PropertiesROptionsImpl : PropertiesROptions {
    override var writeResourceBundle: Boolean = false
}
