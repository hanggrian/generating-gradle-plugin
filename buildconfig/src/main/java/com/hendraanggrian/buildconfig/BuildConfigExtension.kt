package com.hendraanggrian.buildconfig

@Suppress("UNUSED")
open class BuildConfigExtension {

    internal var mFields = linkedMapOf<String, Pair<Class<*>, Any>>()

    /** Package name of generated class, optional. */
    var packageName: String = "buildconfig"

    /** Name of which class will be generated with, optional. */
    var className: String = "BuildConfig"

    /** Path of which BuildConfig class is generated to. */
    var srcDir: String = "src/main/java"

    /** Add any field with specified name, type and value. */
    fun <T : Any> field(name: String, type: Class<T>, value: T) = mFields.put(name, Pair(type, value))

    fun groupId(groupId: String) = field("GROUP", String::class.java, groupId)

    fun artifactId(artifactId: String) = field("ARTIFACT", String::class.java, artifactId)

    fun version(version: String) = field("VERSION", String::class.java, version)

    fun debug(debug: Boolean) = field("DEBUG", Boolean::class.java, debug)
}