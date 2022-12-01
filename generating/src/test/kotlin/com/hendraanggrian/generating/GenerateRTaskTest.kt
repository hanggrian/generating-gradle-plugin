package com.hendraanggrian.generating

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.util.Properties
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateRTaskTest {
    @Rule @JvmField
    val testProjectDir = TemporaryFolder()
    private lateinit var buildFile: File
    private lateinit var runner: GradleRunner

    @BeforeTest
    @Throws(IOException::class)
    fun setup() {
        testProjectDir.newFolder("src", "main", "resources").resolve("my.properties").outputStream()
            .use { stream -> Properties().apply { setProperty("key", "value") }.store(stream, "") }
        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            rootProject.name = "generate-r-test"
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
            tasks.generateBuildConfig {
                enabled = false
            }
            // temporary until TODO is fixed
            tasks.generateR {
                packageName.set("com.example")
                properties()
            }
            """.trimIndent()
        )
        assertEquals(
            TaskOutcome.SUCCESS,
            runner.withArguments(GeneratingPlugin.TASK_GENERATE_R).build()
                .task(":${GeneratingPlugin.TASK_GENERATE_R}")!!.outcome
        )
        assertTrue(
            !testProjectDir.root.resolve("build/generated/java/com/example/BuildConfig.java")
                .exists()
        )
        testProjectDir.root.resolve("build/generated/java/com/example/R.java").readLines().let {
            assertTrue("package com.example;" in it, "invalid package")
            assertTrue("public final class R {" in it, "invalid class")
            assertTrue("  public static final String key = \"key\";" in it, "invalid properties")
            assertTrue(
                "  public static final String _my = \"/my.properties\";" in it,
                "invalid path"
            )
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
                enabled = false
            }
            tasks.generateR {
                properties()
                packageName.set("mypackage")
                className.set("R2")
                shouldUppercaseField.set(true)
                shouldLowercaseClass.set(true)
            }
            """.trimIndent()
        )
        assertEquals(
            TaskOutcome.SUCCESS,
            runner.withArguments(GeneratingPlugin.TASK_GENERATE_R).build()
                .task(":${GeneratingPlugin.TASK_GENERATE_R}")!!.outcome
        )
        assertTrue(
            !testProjectDir.root.resolve("build/generated/java/mypackage/BuildConfig.java").exists()
        )
        testProjectDir.root.resolve("build/generated/java/mypackage/R2.java").readLines().let {
            assertTrue("package mypackage;" in it, "invalid package")
            assertTrue("public final class r2 {" in it, "invalid class")
            assertTrue("  public static final String KEY = \"key\";" in it, "invalid properties")
            assertTrue(
                "  public static final String _MY = \"/my.properties\";" in it,
                "invalid path"
            )
        }
    }
}
