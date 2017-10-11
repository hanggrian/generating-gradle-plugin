package com.hendraanggrian.buildconfig

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
open class BuildConfigExtension {

    /** Package name of generated class, optional. */
    var packageName: String = "buildconfig"

    /** Name of which class will be generated with, optional. */
    var className: String = "BuildConfig"

    /** Path of which BuildConfig class is generated to. */
    var srcDir: String = "src/main/java"

    var groupId: String = "unspecified"
    var artifactId: String = "unspecified"
    var version: String = "unspecified"
    var debug: Boolean = false
}