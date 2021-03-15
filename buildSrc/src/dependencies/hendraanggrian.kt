const val VERSION_JAVAPOETKTX = "0.1-SNAPSHOT"

fun org.gradle.api.artifacts.dsl.DependencyHandler.hendraanggrian(
    module: String,
    version: String
) = "io.github.hendraanggrian:$module:$version"

fun org.gradle.api.artifacts.dsl.DependencyHandler.hendraanggrian(
    repo: String,
    module: String,
    version: String
) = "io.github.hendraanggrian.$repo:$module:$version"

fun org.gradle.plugin.use.PluginDependenciesSpec.hendraanggrian(module: String) =
    id("io.github.hendraanggrian.$module")