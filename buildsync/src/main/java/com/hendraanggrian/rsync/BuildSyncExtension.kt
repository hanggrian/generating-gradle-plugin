package com.hendraanggrian.rsync

/**
 * Extension to customize rsync generation, note that all of this is optional.
 *
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
open class BuildSyncExtension {

    /** Package name of generated class, optional. */
    var packageName: String = "rsync"

    /** Name of which class will be generated with, optional. */
    var className: String = "R"

    /** Automatic detection not yet supported, relies for absolute path for now. */
    var pathToResources: String = "src/main/resources"

    /** Automatic detection not yet supported, relies for absolute path for now. */
    var pathToJava: String = "src/main/java"

    /** Skips these properties files, optional. */
    var ignore: Array<String> = emptyArray()

    /** Will add '/' prefix to non-properties resources. */
    var leadingSlash: Boolean = false
}