package com.hendraanggrian.generating.adapters

import com.hendraanggrian.generating.RPropertiesConfiguration
import com.hendraanggrian.javapoet.TypeSpecBuilder
import org.gradle.api.logging.Logger
import java.io.File
import java.util.Properties
import javax.lang.model.element.Modifier

/**
 * An adapter that writes [Properties] keys.
 * The file path itself will be written with underscore prefix.
 */
internal class PropertiesAdapter(
    private val configuration: RPropertiesConfiguration,
    private val isLowercaseClass: Boolean,
    isUppercaseField: Boolean,
    logger: Logger
) : BaseAdapter(isUppercaseField, logger) {

    override fun process(typeBuilder: TypeSpecBuilder, file: File): Boolean {
        logger.debug("File '${file.name}' is recognized as properties.")
        if (file.extension == "properties") {
            when {
                configuration.isWriteResourceBundle && file.isResourceBundle() -> {
                    var className = file.resourceBundleName
                    if (isLowercaseClass) {
                        className = className.toLowerCase()
                    }
                    if (className !in typeBuilder.build().typeSpecs.map { it.name }) {
                        typeBuilder.types.addClass(className) {
                            addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            methods.addConstructor { addModifiers(Modifier.PRIVATE) }
                            file.forEachKey { addField(it) }
                        }
                    }
                }
                else -> file.forEachKey { typeBuilder.addField(it) }
            }
            return true
        }
        return false
    }

    private fun File.forEachKey(action: (String) -> Unit) = inputStream().use { stream ->
        Properties().run {
            load(stream)
            keys.map { it as? String ?: it.toString() }.forEach(action)
        }
    }

    private val File.resourceBundleName: String get() = nameWithoutExtension.substringBeforeLast("_")

    private fun File.isResourceBundle(): Boolean = !isHidden &&
        extension == "properties" &&
        nameWithoutExtension.let { name -> '_' in name && name.substringAfterLast("_").length == 2 }
}
