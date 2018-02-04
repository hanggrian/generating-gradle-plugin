const val bintrayUser = "hendraanggrian"
const val bintrayGroup = "com.hendraanggrian"
const val bintrayArtifact = "buildconfig"
const val bintrayPublish = "0.8"
const val bintrayDesc = "BuildConfig class for non-Android Java projects"
const val bintrayWeb = "https://github.com/$bintrayUser/$bintrayArtifact"

const val kotlinVersion = "1.2.21"

val Dependency.dokka get() = "org.jetbrains.dokka:dokka-gradle-plugin:0.9.15"
val Plugin.dokka get() = id("org.jetbrains.dokka")

val Dependency.`bintray-release` get() = "com.novoda:bintray-release:0.8.0"
val Plugin.`bintray-release` get() = id("com.novoda.bintray-release")

fun Dependency.javapoet() = "com.squareup:javapoet:1.10.0"

fun Dependency.junitPlatform(module: String, version: String) = "org.junit.platform:junit-platform-$module:$version"
val Plugin.`junit-platform` get() = id("org.junit.platform.gradle.plugin")
const val junitPlatformVersion = "1.0.0"

fun Dependency.spek(module: String, version: String) = "org.jetbrains.spek:spek-$module:$version"
const val spekVersion = "1.1.5"

private typealias Dependency = org.gradle.api.artifacts.dsl.DependencyHandler
private typealias Plugin = org.gradle.plugin.use.PluginDependenciesSpec
