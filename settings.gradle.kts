include("generating-gradle-plugin")
include("website")
includeDir("samples")

fun includeDir(dir: String) = file(dir)
    .listFiles()
    .filter { it.isDirectory }
    .forEach { include("$dir:${it.name}") }

dependencyResolutionManagement {
    versionCatalogs {
        val kotlinVersion = "1.6.21"
        register("plugs") {
            val spotlessVersion = "6.7.0"
            val pluginPublishVersion = "1.0.0-rc-2"
            val pagesVersion = "0.1"
            val gitPublishVersion = "3.0.1"
            library("kotlin", "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
            library("dokka", "org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVersion")
            library("spotless", "com.diffplug.spotless:spotless-plugin-gradle:$spotlessVersion")
            library("plugin-publish", "com.gradle.publish:plugin-publish-plugin:$pluginPublishVersion")
            library("pages", "com.hendraanggrian:pages-gradle-plugin:$pagesVersion")
            library("git-publish", "org.ajoberstar.git-publish:gradle-git-publish:$gitPublishVersion")
        }
        register("libs") {
            val coroutinesVersion = "1.6.2"
            val javapoetDslVersion = "0.1-SNAPSHOT"
            val jsonSimpleVersion = "1.1.1"
            val phCssVersion = "6.4.0"
            library("kotlinx-coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            library("javapoet-dsl", "com.hendraanggrian:javapoet-dsl:$javapoetDslVersion")
            library("json-simple", "com.googlecode.json-simple:json-simple:$jsonSimpleVersion")
            library("ph-css", "com.helger:ph-css:$phCssVersion")
        }
        register("testLibs") {
            val truthVersion = "1.1.3"
            library("kotlin-junit", "org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
            library("truth", "com.google.truth:truth:$truthVersion")
        }
    }
}
