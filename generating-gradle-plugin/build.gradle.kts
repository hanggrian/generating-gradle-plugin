group = RELEASE_GROUP
version = RELEASE_VERSION

plugins {
    idea
    `java-gradle-plugin`
    `kotlin-dsl`
    dokka
    `gradle-publish`
}

idea

sourceSets {
    main {
        java.srcDir("src")
    }
    register("functionalTest") {
        java.srcDir("functional-tests/src")
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath
        runtimeClasspath += output + compileClasspath
    }
}

gradlePlugin {
    plugins {
        val generatingPlugin by plugins.registering {
            id = "$RELEASE_GROUP.generating"
            implementationClass = "$RELEASE_GROUP.generating.GeneratingPlugin"
            displayName = "Generating Plugin"
            description = RELEASE_DESCRIPTION
        }
    }
    testSourceSets(sourceSets["functionalTest"])
}

ktlint()

dependencies {
    implementation(kotlin("stdlib", VERSION_KOTLIN))
    implementation(hendraanggrian("javapoet-ktx", VERSION_JAVAPOETKTX))
    implementation(phCss())
    implementation(jsonSimple())
    "functionalTestImplementation"(gradleTestKit())
    "functionalTestImplementation"(kotlin("test-junit", VERSION_KOTLIN))
}

tasks {
    val functionalTest by registering(Test::class) {
        description = "Runs the functional tests."
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        testClassesDirs = sourceSets["functionalTest"].output.classesDirs
        classpath = sourceSets["functionalTest"].runtimeClasspath
        mustRunAfter(test)
    }
    check { dependsOn(functionalTest) }
}

pluginBundle {
    website = RELEASE_GITHUB
    vcsUrl = "$RELEASE_GITHUB.git"
    description = RELEASE_DESCRIPTION
    tags = listOf("generating", "buildconfig", "r")
}