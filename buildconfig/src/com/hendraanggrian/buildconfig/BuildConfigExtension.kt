package com.hendraanggrian.buildconfig

import org.gradle.api.Named
import javax.lang.model.SourceVersion.isName
import kotlin.DeprecationLevel.ERROR

/** Extension to customize BuildConfig generation, note that all properties are optional. */
open class BuildConfigExtension {

    internal var fields: MutableMap<String, Pair<Class<*>, Any>> = linkedMapOf()

    var name: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(String::class.java, FIELD_NAME, value)

    var group: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(String::class.java, FIELD_GROUP, value)

    var version: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(String::class.java, FIELD_VERSION, value)

    var debug: Boolean
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(Boolean::class.java, FIELD_DEBUG, value)

    /** Add custom field specifying its type, name, and value. */
    fun <T : Any> field(type: Class<T>, name: String, value: T) {
        require(isName(name)) { "Field name is not a valid java variable name" }
        fields[name] = type to value
    }

    internal val packageName: String
        get() = when (fields.contains(FIELD_GROUP)) {
            true -> fields[FIELD_GROUP]!!.second as String
            else -> getName()
        }

    companion object : Named {
        override fun getName(): String = "buildconfig"

        internal const val FIELD_NAME = "NAME"
        internal const val FIELD_GROUP = "GROUP"
        internal const val FIELD_VERSION = "VERSION"
        internal const val FIELD_DEBUG = "DEBUG"

        private const val NO_GETTER: String = "Property does not have a getter"

        private fun noGetter(): Nothing = throw UnsupportedOperationException(NO_GETTER)
    }
}