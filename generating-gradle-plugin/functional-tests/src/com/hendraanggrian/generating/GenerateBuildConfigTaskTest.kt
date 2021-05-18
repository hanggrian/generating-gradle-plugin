package com.hendraanggrian.generating

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateBuildConfigTaskTest {

    @Rule @JvmField val testProjectDir = TemporaryFolder()
    private lateinit var settingsFile: File
    private lateinit var buildFile: File
    private lateinit var runner: GradleRunner

    @BeforeTest
    @Throws(IOException::class)
    fun setup() {
        settingsFile = testProjectDir.newFile("settings.gradle.kts")
        settingsFile.writeText(
            """
            rootProject.name = "functional-test"
            """.trimIndent()
        )
        buildFile = testProjectDir.newFile("build.gradle.kts")
        runner = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withTestKitDir(testProjectDir.newFolder())
    }

    @Test
    fun noConfiguration() {
        // TODO: find out why group and version are always null
        buildFile.writeText(
            """
            group = "com.example"
            version = "1.0"
            plugins {
                java
                id("com.hendraanggrian.generating")
            }
            tasks.generateR {
                setEnabled(false)
            }
            """.trimIndent()
        )
        runner.withArguments("compileBuildConfig").build().let {
            assertEquals(TaskOutcome.SUCCESS, it.task(":compileBuildConfig")!!.outcome)
            val lines = testProjectDir.root.resolve("build/generated/buildconfig/src/main/com/example/BuildConfig.java")
                .readLines()
            assertTrue("package com.example;" in lines, "invalid package")
            assertTrue("public final class BuildConfig {" in lines, "invalid class")
            assertTrue("  public static final String NAME = \"functional-test\";" in lines, "invalid name")
            assertTrue("  public static final String GROUP = \"com.example\";" in lines, "invalid group")
            assertTrue("  public static final String VERSION = \"1.0\";" in lines, "invalid version")
            assertTrue("  public static final Boolean DEBUG = false;" in lines, "invalid debug")
        }
    }

    @Test
    fun configureAll() {
        buildFile.writeText(
            """
            group = "com.example"
            version = "1.0"
            plugins {
                java
                id("com.hendraanggrian.generating")
            }
            tasks.generateBuildConfig {
                packageName.set("mypackage")
                className.set("Build")
                appName.set("My App")
                groupId.set("my.website")
                version.set("2.0")
                debug.set(true)
                desc.set("Awesome app")
                email.set("me@mail.com")
                url.set("https://my.website")
            }
            tasks.generateR {
                setEnabled(false)
            }
            """.trimIndent()
        )
        runner.withArguments("compileBuildConfig").build().let {
            assertEquals(TaskOutcome.SUCCESS, it.task(":compileBuildConfig")!!.outcome)
            val lines = testProjectDir.root.resolve("build/generated/buildconfig/src/main/mypackage/Build.java")
                .readLines()
            assertTrue("" in lines, "invalid class")
            assertTrue("package mypackage;" in lines, "invalid package")
            assertTrue("public final class Build {" in lines, "invalid class")
            assertTrue("  public static final String NAME = \"My App\";" in lines, "invalid name")
            assertTrue("  public static final String GROUP = \"my.website\";" in lines, "invalid group")
            assertTrue("  public static final String VERSION = \"2.0\";" in lines, "invalid version")
            assertTrue("  public static final Boolean DEBUG = true;" in lines, "invalid debug")
            assertTrue("  public static final String DESC = \"Awesome app\";" in lines, "invalid version")
            assertTrue("  public static final String EMAIL = \"me@mail.com\";" in lines, "invalid version")
            assertTrue("  public static final String URL = \"https://my.website\";" in lines, "invalid version")
        }
    }
}