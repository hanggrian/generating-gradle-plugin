group = RELEASE_GROUP
version = RELEASE_VERSION

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    dokka
    `gradle-publish`
}

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
    testSourceSets(sourceSets["functionalTest"])
}

dependencies {
    implementation(kotlin("stdlib", VERSION_KOTLIN))
    implementation(hendraanggrian("javapoet-ktx", VERSION_JAVAPOETKTX))
    "functionalTestImplementation"(gradleTestKit())
    "functionalTestImplementation"(kotlin("test-junit", VERSION_KOTLIN))
}

ktlint()

tasks {
    val deploy by registering {
        dependsOn("build")
        projectDir.resolve("build/libs").listFiles()?.forEach {
            it.renameTo(File(rootDir.resolve("example"), it.name))
        }
    }

    val functionalTest by registering(Test::class) {
        description = "Runs the functional tests."
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        testClassesDirs = sourceSets["functionalTest"].output.classesDirs
        classpath = sourceSets["functionalTest"].runtimeClasspath
        mustRunAfter(test)
    }
    check { dependsOn(functionalTest) }
}

publishPlugin(
    "BuildConfig Gradle Plugin",
    "$RELEASE_GROUP.$RELEASE_ARTIFACT.BuildConfigPlugin"
)