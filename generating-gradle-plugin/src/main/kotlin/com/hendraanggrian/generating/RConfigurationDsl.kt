package com.hendraanggrian.generating

/**
 * Forces R file type configurations to be on the same level, such as:
 *
 * ```
 * tasks.generateR {
 *     css { }
 *     json { }
 *     properties { }
 * }
 * ```
 */
@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class RConfigurationDsl
