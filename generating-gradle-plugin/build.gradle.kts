plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(plugs.plugins.kotlin.jvm)
    alias(plugs.plugins.dokka)
    alias(plugs.plugins.spotless)
    alias(plugs.plugins.gradle.publish)
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

kotlin.jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(sdk.versions.jdk.get()))
}

spotless.kotlin {
    ktlint()
}

pluginBundle {
    website = RELEASE_URL
    vcsUrl = "$RELEASE_URL.git"
    description = RELEASE_DESCRIPTION
    tags = listOf("generating", "codegen", "buildconfig", "r")
}

val cssImplementation by configurations.getting
val jsonImplementation by configurations.getting

dependencies {
    implementation(libs.javapoet.dsl)
    cssImplementation(libs.javapoet.dsl)
    cssImplementation(libs.ph.css)
    jsonImplementation(libs.javapoet.dsl)
    jsonImplementation(libs.json.simple)
    testImplementation(gradleTestKit())
    testImplementation(testLibs.kotlin.junit)
}

tasks.dokkaHtml {
    outputDirectory.set(buildDir.resolve("dokka/dokka"))
}
