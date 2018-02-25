import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.kotlin
import java.io.File

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath(dokka())
        classpath(gitPublish())
        classpath(bintrayRelease())
        classpath(junitPlatform("gradle-plugin", junitPlatformVersion))
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
    tasks.withType(Delete::class.java) {
        delete(File(projectDir, "out"))
    }
}

tasks {
    "clean"(Delete::class) {
        delete(rootProject.buildDir)
    }
    "wrapper"(Wrapper::class) {
        gradleVersion = "4.4.1"
    }
}

/** bintray upload snippet
./gradlew bintrayUpload -PdryRun=false -PbintrayUser=hendraanggrian -PbintrayKey=
 */
