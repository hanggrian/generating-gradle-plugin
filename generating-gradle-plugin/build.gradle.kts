val releaseGroup: String by project
val releaseDescription: String by project
val releaseUrl: String by project

plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.gradle.publish)
}

kotlin.jvmToolchain(libs.versions.jdk.get().toInt())

gradlePlugin {
    website.set(releaseUrl)
    vcsUrl.set("$releaseUrl.git")
    plugins.register("generatingPlugin") {
        id = releaseGroup
        displayName = "Generating Plugin"
        description = releaseDescription
        tags.set(listOf("generating", "codegen", "buildconfig", "r"))
        implementationClass = "$releaseGroup.GeneratingPlugin"
    }
    testSourceSets(sourceSets.test.get())
}

dependencies {
    ktlintRuleset(libs.rulebook.ktlint)

    compileOnly(kotlin("gradle-plugin-api"))

    implementation(gradleKotlinDsl())
    implementation(libs.javapoet.dsl)
    implementation(libs.ph.css)
    implementation(libs.json.simple)

    testImplementation(gradleTestKit())
    testImplementation(kotlin("test-junit", libs.versions.kotlin.get()))
}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("dokka/dokka/"))
}
