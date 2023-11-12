val releaseGroup: String by project
val releaseVersion: String by project

plugins {
    kotlin("jvm") version libs.versions.kotlin apply false
    alias(libs.plugins.ktlint) apply false
}

allprojects {
    group = releaseGroup
    version = releaseVersion
}

subprojects {
    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper>().configureEach {
        the<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>()
            .jvmToolchain(libs.versions.jdk.get().toInt())
    }
    plugins.withType<org.jlleitschuh.gradle.ktlint.KtlintPlugin>().configureEach {
        the<org.jlleitschuh.gradle.ktlint.KtlintExtension>()
            .version.set(libs.versions.ktlint.get())
    }
}
