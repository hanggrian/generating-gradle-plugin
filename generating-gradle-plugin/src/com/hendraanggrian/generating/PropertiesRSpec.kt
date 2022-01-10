package com.hendraanggrian.generating

import org.gradle.api.provider.Property

/** Settings for customizing the field generation of properties file. */
interface PropertiesRSpec {

    /**
     * Determine whether adapter should also write resource bundle.
     * Default is false.
     */
    var writeResourceBundle: Property<Boolean>
}
