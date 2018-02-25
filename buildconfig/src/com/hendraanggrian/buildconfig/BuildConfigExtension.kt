package com.hendraanggrian.buildconfig

import com.hendraanggrian.buildconfig.BuildConfigWriter.Companion.FIELD_DEBUG
import com.hendraanggrian.buildconfig.BuildConfigWriter.Companion.FIELD_GROUP
import com.hendraanggrian.buildconfig.BuildConfigWriter.Companion.FIELD_NAME
import com.hendraanggrian.buildconfig.BuildConfigWriter.Companion.FIELD_VERSION
import org.gradle.api.Project
import javax.lang.model.SourceVersion.isName
import kotlin.DeprecationLevel.ERROR

/** Extension to customize BuildConfig generation, note that all properties are optional. */
open class BuildConfigExtension {

    private var _packageName: String? = null
    private var _className: String = "BuildConfig"
    private var _fields: MutableMap<String, Pair<Class<*>, Any>> = mutableMapOf()

    /**
     * Package name of which `buildconfig` class will be generated to.
     * Default is project group.
     */
    var packageName: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) {
            _packageName = value
        }

    /**
     * `buildconfig` generated class name.
     * Default is `BuildConfig`.
     */
    var className: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) {
            _className = value
        }

    /**
     * Customize `BuildConfig.NAME` value.
     * Default is project name.
     */
    var name: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(FIELD_NAME, String::class.java, value)

    /**
     * Customize `BuildConfig.GROUP` value.
     * Default is project group.
     */
    var group: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(FIELD_GROUP, String::class.java, value)

    /**
     * Customize `BuildConfig.VERSION` value.
     * Default is project version.
     */
    var version: String
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(FIELD_VERSION, String::class.java, value)

    /**
     * Customize `BuildConfig.DEBUG` value.
     */
    var debug: Boolean
        @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
        set(value) = field(FIELD_DEBUG, Boolean::class.java, value)

    /**
     * Add custom field specifying its type, name, and value.
     *
     * @param type field type, only primitives are currently supported.
     * @param name field name, must be a valid java variable name.
     * @param value non-null field value.
     */
    fun <T : Any> field(name: String, type: Class<T>, value: T) {
        require(isName(name)) { "Field name is not a valid java variable name" }
        _fields[name] = type to value
    }

    /** Value of project's group and version might change upon completion of project evaluation. */
    internal fun applyDefault(project: Project) {
        if (_packageName == null) packageName = project.group.toString()
        if (FIELD_NAME !in _fields) name = project.name
        if (FIELD_GROUP !in _fields) group = project.group.toString()
        if (FIELD_VERSION !in _fields) version = project.version.toString()
    }

    /** Make task name based on current class name. */
    internal fun getTaskName(prefix: String) = "$prefix$_className"

    /** Convert this extension to class writer. */
    internal fun toWriter() = BuildConfigWriter(_packageName!!, _className, _fields)

    companion object {
        private const val NO_GETTER: String = "Property does not have a getter"

        private fun noGetter(): Nothing = throw UnsupportedOperationException(NO_GETTER)
    }
}