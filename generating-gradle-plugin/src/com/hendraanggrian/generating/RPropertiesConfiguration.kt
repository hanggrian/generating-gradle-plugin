package com.hendraanggrian.generating

/** Settings for customizing the field generation of properties file. */
interface RPropertiesConfiguration {

    /**
     * Determine whether adapter should also write resource bundle.
     * Default is false.
     */
    var isWriteResourceBundle: Boolean
}
