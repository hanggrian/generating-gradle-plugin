group = RELEASE_GROUP
version = RELEASE_VERSION

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    dokka
    `maven-publish`
    signing
}

sourceSets {
    getByName("main") {
        java.srcDir("src")
    }
    getByName("test") {
        java.srcDir("tests/src")
    }
}

gradlePlugin {
    plugins {
        register(RELEASE_ARTIFACT) {
            id = "$RELEASE_GROUP.buildconfig"
            implementationClass = "$id.BuildConfigPlugin"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib", VERSION_KOTLIN))
    implementation(hendraanggrian("javapoet-ktx", VERSION_JAVAPOETKTX))
    testImplementation(kotlin("test-junit", VERSION_KOTLIN))
}

ktlint()

tasks {
    val deploy by registering {
        dependsOn("build")
        projectDir.resolve("build/libs").listFiles()?.forEach {
            it.renameTo(File(rootDir.resolve("integration-tests"), it.name))
        }
    }
    dokkaJavadoc {
        dokkaSourceSets {
            "main" {
                sourceLink {
                    localDirectory.set(projectDir.resolve("src"))
                    remoteUrl.set(getReleaseSourceUrl())
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }
    val dokkaJar by registering(Jar::class) {
        archiveClassifier.set("javadoc")
        from(dokkaJavadoc)
        dependsOn(dokkaJavadoc)
    }
    val sourcesJar by registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }
}

publishJvm()