package com.hendraanggrian.buildconfig

/** Extension to customize BuildConfig generation, note that all properties are optional. */
open class BuildConfigExtension {

    internal var packageName: String = "buildconfig"
    internal var className: String = "BuildConfig"
    internal var srcDir: String = "src/main/java"
    internal var fields = linkedMapOf<String, Pair<Class<*>, Any>>()

    /** Package name of generated class, optional. */
    fun packageName(name: String) {
        packageName = name
    }

    /** Name of which class will be generated with, optional. */
    fun className(name: String) {
        className = name
    }

    /** Path of which BuildConfig class is generated to. */
    fun srcDir(dir: String) {
        srcDir = dir
    }

    /** Add any field with specified name, type and value. */
    fun <T : Any> field(name: String, type: Class<T>, value: T) {
        fields[name] = type to value
    }

    fun groupId(groupId: String) = field("GROUP", String::class.java, groupId)

    fun artifactId(artifactId: String) = field("ARTIFACT", String::class.java, artifactId)

    fun version(version: String) = field("VERSION", String::class.java, version)

    fun debug(debug: Boolean) = field("DEBUG", Boolean::class.java, debug)
}