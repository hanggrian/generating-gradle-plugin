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

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

/** bintray upload snippet
./gradlew bintrayUpload -PdryRun=false -PbintrayUser=hendraanggrian -PbintrayKey=
 */
