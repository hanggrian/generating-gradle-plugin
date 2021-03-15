package io.github.hendraanggrian.buildconfig

import javax.lang.model.SourceVersion
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FieldNameTest {

    @Test fun names() {
        assertFalse(SourceVersion.isName(""))
        assertTrue(SourceVersion.isName("hello"))
        assertFalse(SourceVersion.isName("1hello"))
        assertTrue(SourceVersion.isName("hello_world"))
        assertFalse(SourceVersion.isName("hello-world"))
    }
}