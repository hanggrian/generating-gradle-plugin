import org.gradle.api.artifacts.dsl.DependencyHandler

const val bintrayUser = "hendraanggrian"
const val bintrayGroup = "com.hendraanggrian"
const val bintrayArtifact = "buildconfig"
const val bintrayPublish = "0.5"
const val bintrayDesc = "BuildConfig class for non-Android Java projects"
const val bintrayWeb = "https://github.com/hendraanggrian/buildconfig"

const val kotlinVersion = "1.1.61"
const val javapoetVersion = "1.9.0"

const val junitVersion = "4.12"

fun DependencyHandler.javapoet(version: String) = "com.squareup:javapoet:$version"
fun DependencyHandler.junit(version: String) = "junit:junit:$version"
