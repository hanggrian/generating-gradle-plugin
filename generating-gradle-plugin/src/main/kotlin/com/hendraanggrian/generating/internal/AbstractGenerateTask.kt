package com.hendraanggrian.generating.internal

import com.hendraanggrian.generating.GenerateBuildConfigTask
import com.hendraanggrian.generating.GenerateRTask
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.kotlin.dsl.property

/**
 * Base task for [GenerateBuildConfigTask] and [GenerateRTask].
 * There is also `className` property which has different convention for each task.
 */
abstract class AbstractGenerateTask : DefaultTask() {
    /**
     * Package name of which `BuildConfig` and `R` class will be generated to, cannot be empty.
     * If left null, project group will be assigned as value.
     */
    @Input
    val packageName: Property<String> = project.objects.property()

    /**
     * Output directory of generated classes.
     * Default is `$projectDir/build/generated/java`.
     */
    @OutputDirectory
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("generated/java"))
}
