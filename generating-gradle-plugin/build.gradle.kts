group = RELEASE_GROUP
version = RELEASE_VERSION

plugins {
    idea
    `java-gradle-plugin`
    `kotlin-dsl`
    dokka
    `gradle-publish`
}

sourceSets {
    main {
        java.srcDir("src")
    }
    test {
        java.srcDir("tests/src")
        resources.srcDir("tests/res")
    }
}

gradlePlugin {
    plugins {
        val generatingPlugin by plugins.registering {
            id = "$RELEASE_GROUP.generating"
            implementationClass = "$RELEASE_GROUP.generating.GeneratingPlugin"
            displayName = "Generating plugin"
            description = RELEASE_DESCRIPTION
        }
    }
    testSourceSets(sourceSets.test.get())
}

ktlint()

dependencies {
    implementation(kotlin("stdlib", VERSION_KOTLIN))
    implementation(hendraanggrian("javapoet-ktx", VERSION_JAVAPOETKTX))
    implementation(phCss())
    implementation(jsonSimple())
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test-junit", VERSION_KOTLIN))
}

tasks {
    dokkaHtml {
        outputDirectory.set(buildDir.resolve("dokka/$RELEASE_ARTIFACT"))
    }
}

pluginBundle {
    website = RELEASE_GITHUB
    vcsUrl = "$RELEASE_GITHUB.git"
    description = RELEASE_DESCRIPTION
    tags = listOf("generating", "codegen", "buildconfig", "r")
    mavenCoordinates {
        groupId = RELEASE_GROUP
        artifactId = RELEASE_ARTIFACT
    }
}