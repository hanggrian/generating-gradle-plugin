package com.hendraanggrian.generating

/** Settings for customizing the field generation of json file. */
interface RJsonConfiguration {

    /**
     * Determine whether adapter should also write inner json object.
     * Default is false.
     */
    var isRecursive: Boolean

    /**
     * Extended property of [isRecursive] that will also write inner json array.
     * Default is true.
     */
    var isWriteArray: Boolean
}
