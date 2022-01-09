package com.hendraanggrian.generating.internal

import com.hendraanggrian.generating.GenerateBuildConfigTask
import com.hendraanggrian.generating.GenerateRTask
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property

/**
 * Base task for [GenerateBuildConfigTask] and [GenerateRTask].
 * There is also `className` property which has different convention for each task.
 */
abstract class AbstractGenerateTask : DefaultTask() {

    /**
     * Determine whether write this class.
     */
    @Input
    val enabled: Property<Boolean> = project.objects.property<Boolean>()
        .convention(true)

    /**
     * Package name of which `R` class will be generated to, cannot be empty.
     * If left null, project group will be assigned as value.
     */
    @Input
    val packageName: Property<String> = project.objects.property()
}
