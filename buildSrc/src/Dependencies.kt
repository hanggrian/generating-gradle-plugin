import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.plugin.use.PluginDependenciesSpec

fun DependencyHandler.dokka() = "org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion"
val PluginDependenciesSpec.dokka get() = id("org.jetbrains.dokka")

fun DependencyHandler.spek(module: String) = "org.jetbrains.spek:spek-$module:$spekVersion"

fun DependencyHandler.gitPublish() = "org.ajoberstar:gradle-git-publish:$gradleGitVersion"
inline val PluginDependenciesSpec.`git-publish` get() = id("org.ajoberstar.git-publish")

fun DependencyHandler.bintrayRelease() = "com.novoda:bintray-release:$bintrayReleaseVersion"
val PluginDependenciesSpec.`bintray-release` get() = id("com.novoda.bintray-release")

fun DependencyHandler.javapoet() = "com.squareup:javapoet:$javapoetVersion"

fun DependencyHandler.junitPlatform(module: String) = "org.junit.platform:junit-platform-$module:$junitPlatformVersion"
val PluginDependenciesSpec.`junit-platform` get() = id("org.junit.platform.gradle.plugin")

fun DependencyHandler.ktlint() = "com.github.shyiko:ktlint:$ktlintVersion"