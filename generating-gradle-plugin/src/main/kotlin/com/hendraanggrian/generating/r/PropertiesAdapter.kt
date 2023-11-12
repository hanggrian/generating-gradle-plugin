package com.hendraanggrian.generating.r

import com.hendraanggrian.javapoet.FINAL
import com.hendraanggrian.javapoet.PRIVATE
import com.hendraanggrian.javapoet.PUBLIC
import com.hendraanggrian.javapoet.STATIC
import com.hendraanggrian.javapoet.TypeSpecBuilder
import com.hendraanggrian.javapoet.classType
import com.hendraanggrian.javapoet.constructorMethod
import org.gradle.api.logging.Logger
import java.io.File
import java.util.Properties

/**
 * An adapter that writes [Properties] keys. The file path itself will be written with underscore
 * prefix.
 */
internal class PropertiesAdapter(
    private val configuration: PropertiesROptions,
    private val isLowercaseClass: Boolean,
    isUppercaseField: Boolean,
    logger: Logger,
) : RAdapter(isUppercaseField, logger) {
    override fun process(typeBuilder: TypeSpecBuilder, file: File): Boolean {
        logger.debug("File '${file.name}' is recognized as properties.")
        if (file.extension == "properties") {
            when {
                configuration.writeResourceBundle && file.isResourceBundle() -> {
                    var className = file.resourceBundleName
                    if (isLowercaseClass) {
                        className = className.lowercase()
                    }
                    if (className !in typeBuilder.build().typeSpecs.map { it.name }) {
                        typeBuilder.classType(className) {
                            modifiers(PUBLIC, STATIC, FINAL)
                            constructorMethod { modifiers(PRIVATE) }
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

    private fun File.forEachKey(action: (String) -> Unit) =
        inputStream().use { stream ->
            Properties().run {
                load(stream)
                keys.map { it as? String ?: it.toString() }.forEach(action)
            }
        }

    private val File.resourceBundleName get() = nameWithoutExtension.substringBeforeLast("_")

    private fun File.isResourceBundle() =
        !isHidden &&
            extension == "properties" &&
            nameWithoutExtension
                .let { name -> '_' in name && name.substringAfterLast("_").length == 2 }
}
