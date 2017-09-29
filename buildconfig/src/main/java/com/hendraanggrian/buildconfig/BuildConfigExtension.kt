package com.hendraanggrian.buildconfig

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
open class BuildConfigExtension {

    /** Package name of generated class, optional. */
    var packageName: String = "buildconfig"

    /** Name of which class will be generated with, optional. */
    var className: String = "BuildConfig"

    /** Automatic detection not yet supported, relies for absolute path for now. */
    var pathToJava: String = "src/main/java"

    var groupId: String? = null
    var artifactId: String? = null
    var version: String? = null
}