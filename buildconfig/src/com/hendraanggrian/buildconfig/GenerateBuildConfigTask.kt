package com.hendraanggrian.buildconfig

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException

open class GenerateBuildConfigTask : DefaultTask() {

    @Input lateinit var writer: BuildConfigWriter
    @OutputDirectory lateinit var outputDirectory: File

    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        outputDirectory.deleteRecursively()
        writer.write(outputDirectory)
    }
}
