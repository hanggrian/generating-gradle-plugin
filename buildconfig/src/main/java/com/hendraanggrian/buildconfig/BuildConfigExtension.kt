package com.hendraanggrian.buildconfig

/** Extension to customize BuildConfig generation, note that all of this is optional. */
@Suppress("UNUSED")
open class BuildConfigExtension {

    internal var mPackageName: String = "buildconfig"
    internal var mClassName: String = "BuildConfig"
    internal var mSrcDir: String = "src/main/java"
    internal var mFields = linkedMapOf<String, Pair<Class<*>, Any>>()

    /** Package name of generated class, optional. */
    fun packageName(name: String) {
        mPackageName = name
    }

    /** Name of which class will be generated with, optional. */
    fun className(name: String) {
        mClassName = name
    }

    /** Path of which BuildConfig class is generated to. */
    fun srcDir(dir: String) {
        mSrcDir = dir
    }

    /** Add any field with specified name, type and value. */
    fun <T : Any> field(name: String, type: Class<T>, value: T) = mFields.put(name, Pair(type, value))

    fun groupId(groupId: String) = field("GROUP", String::class.java, groupId)

    fun artifactId(artifactId: String) = field("ARTIFACT", String::class.java, artifactId)

    fun version(version: String) = field("VERSION", String::class.java, version)

    fun debug(debug: Boolean) = field("DEBUG", Boolean::class.java, debug)
}