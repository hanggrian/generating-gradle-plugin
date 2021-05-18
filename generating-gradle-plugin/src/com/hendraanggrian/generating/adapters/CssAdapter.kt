package com.hendraanggrian.generating.adapters

import com.helger.css.reader.CSSReader
import com.hendraanggrian.generating.RCssConfiguration
import com.hendraanggrian.javapoet.TypeSpecBuilder
import org.gradle.api.logging.Logger
import java.io.File

/**
 * An adapter that writes CSS classes and identifiers.
 * The file path itself will be written with underscore prefix.
 */
internal class CssAdapter(
    private val configuration: RCssConfiguration,
    isUppercaseField: Boolean,
    logger: Logger
) : BaseAdapter(isUppercaseField, logger) {

    override fun process(typeBuilder: TypeSpecBuilder, file: File): Boolean {
        logger.debug("File '${file.name}' is recognized as CSS.")
        if (file.extension == "css") {
            val css = checkNotNull(CSSReader.readFromFile(file, configuration.charset, configuration.cssVersion)) {
                "Error while reading CSS, please report to github.com/hendraanggrian/r-gradle-plugin/issues"
            }
            css.allStyleRules.forEach { rule ->
                rule.allSelectors.forEach { selector ->
                    val member = selector.getMemberAtIndex(0)?.asCSSString ?: return false
                    when {
                        member.startsWith('.') -> if (configuration.isWriteClassSelector) {
                            typeBuilder.addField(member.substringAfter('.'))
                        }
                        member.startsWith('#') -> if (configuration.isWriteIdSelector) {
                            typeBuilder.addField(member.substringAfter('#'))
                        }
                        else -> if (configuration.isWriteElementTypeSelector) {
                            typeBuilder.addField(member)
                        }
                    }
                }
            }
            return true
        }
        return false
    }
}
