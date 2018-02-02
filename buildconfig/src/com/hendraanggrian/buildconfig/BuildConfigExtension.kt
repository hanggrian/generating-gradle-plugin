package com.hendraanggrian.buildconfig

import javax.lang.model.SourceVersion.isName

/** Extension to customize BuildConfig generation, note that all properties are optional. */
open class BuildConfigExtension {

    internal var packageName: String = "buildconfig"
    internal var srcDir: String = "src/main/java"
    internal var fieldMap: MutableMap<String, Pair<Class<*>, Any>> = linkedMapOf()

    /** Package name of generated class, optional. */
    fun packageName(name: String) {
        packageName = name
    }

    /** Path of which BuildConfig class is generated to. */
    fun srcDir(dir: String) {
        srcDir = dir
    }

    fun <T : Any> field(type: Class<T>, name: String, value: T) {
        require(isName(name)) { "Field name is not a valid java variable name!" }
        fieldMap[name] = type to value
    }

    fun groupId(groupId: String) = field(String::class.java, "GROUP", groupId)

    fun artifactId(artifactId: String) = field(String::class.java, "ARTIFACT", artifactId)

    fun version(version: String) = field(String::class.java, "VERSION", version)

    fun debug(debug: Boolean) = field(Boolean::class.java, "DEBUG", debug)
}