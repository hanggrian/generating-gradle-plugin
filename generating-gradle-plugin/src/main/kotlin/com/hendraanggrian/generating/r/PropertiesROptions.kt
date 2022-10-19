package com.hendraanggrian.generating.r

import com.hendraanggrian.generating.RConfigurationDsl

/**
 * Properties configuration options using [com.hendraanggrian.generating.GenerateRTask.properties].
 * Keys will be generated into fields.
 */
@RConfigurationDsl
interface PropertiesROptions {
    /** Determine whether adapter should also write resource bundle. Default is false. */
    var writeResourceBundle: Boolean
}

internal class PropertiesROptionsImpl : PropertiesROptions {
    override var writeResourceBundle: Boolean = false
}
