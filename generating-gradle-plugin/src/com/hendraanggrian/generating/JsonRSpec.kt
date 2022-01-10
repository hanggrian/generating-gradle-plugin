package com.hendraanggrian.generating

import org.gradle.api.provider.Property

/** Settings for customizing the field generation of json file. */
interface JsonRSpec {

    /**
     * Determine whether adapter should also write inner json object.
     * Default is false.
     */
    var recursive: Property<Boolean>

    /**
     * Extended property of [recursive] that will also write inner json array.
     * Default is true.
     */
    var writeArray: Property<Boolean>
}
