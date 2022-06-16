package com.hendraanggrian.generating.r

import com.hendraanggrian.generating.RConfigurationDsl

/** A specification for field generation of JSON file. */
@RConfigurationDsl
interface JsonROptions {
    /**
     * Determine whether adapter should also write inner json object.
     * Default is false.
     */
    var recursive: Boolean

    /**
     * Extended property of [recursive] that will also write inner json array.
     * Default is true.
     */
    var writeArray: Boolean
}

internal class JsonROptionsImpl : JsonROptions {
    override var recursive: Boolean = false
    override var writeArray: Boolean = true
}
