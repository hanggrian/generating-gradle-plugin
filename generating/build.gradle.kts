plugins {
    `java-gradle-plugin`
    `kotlin-dsl` version libs.versions.gradle.kotlin.dsl
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.dokka)
    alias(libs.plugins.gradle.publish)
}

java {
    registerFeature("css") {
        usingSourceSet(sourceSets.main.get())
        capability(RELEASE_GROUP, "generating-css", RELEASE_VERSION)
    }
    registerFeature("json") {
        usingSourceSet(sourceSets.main.get())
        capability(RELEASE_GROUP, "generating-json", RELEASE_VERSION)
    }
}

gradlePlugin {
    plugins.register("generatingPlugin") {
        id = "$RELEASE_GROUP.generating"
        implementationClass = "$id.GeneratingPlugin"
        displayName = "Generating Plugin"
        description = RELEASE_DESCRIPTION
    }
    testSourceSets(sourceSets.test.get())
}

kotlin.jvmToolchain(libs.versions.jdk.get().toInt())

pluginBundle {
    website = RELEASE_URL
    vcsUrl = "$RELEASE_URL.git"
    description = RELEASE_DESCRIPTION
    tags = listOf("generating", "codegen", "buildconfig", "r")
}

val cssImplementation by configurations.getting
val jsonImplementation by configurations.getting

dependencies {
    ktlint(libs.ktlint, ::ktlintConfig)
    ktlint(libs.rulebook.ktlint)
    implementation(libs.javapoet.dsl)
    cssImplementation(libs.javapoet.dsl)
    cssImplementation(libs.ph.css)
    jsonImplementation(libs.javapoet.dsl)
    jsonImplementation(libs.json.simple)
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test-junit", libs.versions.kotlin.get()))
}

tasks.dokkaHtml {
    outputDirectory.set(buildDir.resolve("dokka/dokka/"))
}
