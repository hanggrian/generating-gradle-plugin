const val VERSION_JAVAPOETKTX = "0.1-SNAPSHOT"

fun org.gradle.api.artifacts.dsl.DependencyHandler.hendraanggrian(
    module: String,
    version: String
) = "com.hendraanggrian:$module:$version"

fun org.gradle.api.artifacts.dsl.DependencyHandler.hendraanggrian(
    repo: String,
    module: String,
    version: String
) = "com.hendraanggrian.$repo:$module:$version"

fun org.gradle.plugin.use.PluginDependenciesSpec.hendraanggrian(module: String) =
    id("com.hendraanggrian.$module")