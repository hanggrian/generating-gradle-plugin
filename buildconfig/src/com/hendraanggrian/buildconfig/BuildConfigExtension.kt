package com.hendraanggrian.buildconfig

import javax.lang.model.SourceVersion.isName
import kotlin.DeprecationLevel.ERROR

/** Extension to customize BuildConfig generation, note that all properties are optional. */
open class BuildConfigExtension {

    /** Package name of generated class. */
    var packageName: String? = null

    internal var fields: MutableMap<String, Pair<Class<*>, Any>> = linkedMapOf()

    var name: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(String::class.java, "NAME", value)

    var group: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(String::class.java, "GROUP", value)

    var version: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(String::class.java, "VERSION", value)

    var debug: Boolean
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(Boolean::class.java, "DEBUG", value)

    /** Add custom field specifying its type, name, and value. */
    fun <T : Any> field(type: Class<T>, name: String, value: T) {
        require(isName(name)) { "Field name is not a valid java variable name" }
        fields[name] = type to value
    }

    companion object {
        private const val NO_GETTER: String = "Property does not have a getter"

        fun noGetter(): Nothing = throw UnsupportedOperationException(NO_GETTER)
    }
}