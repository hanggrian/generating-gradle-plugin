package com.hendraanggrian.buildconfig

import com.hendraanggrian.buildconfig.BuildConfigField.Companion.NAME_DEBUG
import com.hendraanggrian.buildconfig.BuildConfigField.Companion.NAME_GROUP
import com.hendraanggrian.buildconfig.BuildConfigField.Companion.NAME_NAME
import com.hendraanggrian.buildconfig.BuildConfigField.Companion.NAME_VERSION
import com.hendraanggrian.buildconfig.BuildConfigPlugin.Companion.EXTENSION_NAME
import javax.lang.model.SourceVersion.isName
import kotlin.DeprecationLevel.ERROR

/** Extension to customize BuildConfig generation, note that all properties are optional. */
open class BuildConfigExtension {

    internal var fields: MutableSet<BuildConfigField<*>> = mutableSetOf()

    /**
     * Customize `BuildConfig.NAME` value, default is project name.
     */
    var name: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(String::class.java, NAME_NAME, value)

    /**
     * Customize `BuildConfig.GROUP` value, default is project group.
     * [group] is also used as package name of which `BuildConfig` will be generated to.
     */
    var group: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(String::class.java, NAME_GROUP, value)

    /**
     * Customize `BuildConfig.VERSION` value, default is project version.
     */
    var version: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(String::class.java, NAME_VERSION, value)

    /**
     * Customize `BuildConfig.DEBUG` value.
     */
    var debug: Boolean
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(Boolean::class.java, NAME_DEBUG, value)

    /**
     * Add custom field specifying its type, name, and value.
     *
     * @param type field type, only primitives are currently supported.
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     */
    fun <T : Any> field(type: Class<T>, name: String, value: T) {
        require(isName(name)) { "Field name is not a valid java variable name" }
        fields.add(BuildConfigField(type, name, value))
    }

    internal val packageName: String
        get() = fields.singleOrNull { it.name == NAME_GROUP }?.value as? String ?: EXTENSION_NAME

    companion object {
        private const val NO_GETTER: String = "Property does not have a getter"

        private fun noGetter(): Nothing = throw UnsupportedOperationException(NO_GETTER)
    }
}