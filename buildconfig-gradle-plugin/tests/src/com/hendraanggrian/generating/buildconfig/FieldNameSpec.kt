package com.hendraanggrian.generating.buildconfig

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import javax.lang.model.SourceVersion.isName
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(JUnitPlatform::class)
object FieldNameSpec : Spek({

    given("some field names") {
        it("is a valid java variable name") {
            assertFalse(isName(""))
            assertTrue(isName("hello"))
            assertFalse(isName("1hello"))
            assertTrue(isName("hello_world"))
            assertFalse(isName("hello-world"))
        }
    }
})