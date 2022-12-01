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
    @Rule @JvmField
    val testProjectDir = TemporaryFolder()
    private lateinit var buildFile: File
    private lateinit var runner: GradleRunner

    @BeforeTest
    @Throws(IOException::class)
    fun setup() {
        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            rootProject.name = "generate-buildconfig-test"
            """.trimIndent()
        )
        buildFile = testProjectDir.newFile("build.gradle.kts")
        runner = GradleRunner.create().withPluginClasspath().withProjectDir(testProjectDir.root)
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
                enabled = false
            }
            // temporary until TODO is fixed
            tasks.generateBuildConfig {
                packageName.set("com.example")
                groupId.set("com.example")
                appVersion.set("1.0")
            }
            """.trimIndent()
        )
        assertEquals(
            TaskOutcome.SUCCESS,
            runner.withArguments(GeneratingPlugin.TASK_GENERATE_BUILDCONFIG).build()
                .task(":${GeneratingPlugin.TASK_GENERATE_BUILDCONFIG}")!!.outcome
        )
        assertTrue(!testProjectDir.root.resolve("build/generated/java/com/example/R.java").exists())
        testProjectDir.root.resolve("build/generated/java/com/example/BuildConfig.java").readLines()
            .let {
                assertTrue("package com.example;" in it, "invalid package")
                assertTrue("public final class BuildConfig {" in it, "invalid class")
                assertTrue(
                    "  public static final String NAME = \"generate-buildconfig-test\";" in it,
                    "invalid name"
                )
                assertTrue(
                    "  public static final String GROUP = \"com.example\";" in it,
                    "invalid group"
                )
                assertTrue(
                    "  public static final String VERSION = \"1.0\";" in it,
                    "invalid version"
                )
                assertTrue("  public static final Boolean DEBUG = false;" in it, "invalid debug")
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
            tasks.generateR {
                enabled = false
            }
            tasks.generateBuildConfig {
                packageName.set("mypackage")
                className.set("Build")
                appName.set("My App")
                appVersion.set("2.0")
                groupId.set("my.website")
                debug.set(true)
            }
            """.trimIndent()
        )
        assertEquals(
            TaskOutcome.SUCCESS,
            runner.withArguments(GeneratingPlugin.TASK_GENERATE_BUILDCONFIG).build()
                .task(":${GeneratingPlugin.TASK_GENERATE_BUILDCONFIG}")!!.outcome
        )
        assertTrue(!testProjectDir.root.resolve("build/generated/java/mypackage/R.java").exists())
        testProjectDir.root.resolve("build/generated/java/mypackage/Build.java").readLines().let {
            assertTrue("" in it, "invalid class")
            assertTrue("package mypackage;" in it, "invalid package")
            assertTrue("public final class Build {" in it, "invalid class")
            assertTrue("  public static final String NAME = \"My App\";" in it, "invalid name")
            assertTrue("  public static final String VERSION = \"2.0\";" in it, "invalid version")
            assertTrue("  public static final Boolean DEBUG = true;" in it, "invalid debug")
            assertTrue(
                "  public static final String GROUP = \"my.website\";" in it,
                "invalid group"
            )
        }
    }
}
