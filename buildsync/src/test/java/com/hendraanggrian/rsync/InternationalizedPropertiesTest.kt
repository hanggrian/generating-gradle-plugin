package com.hendraanggrian.rsync

import com.google.common.collect.LinkedHashMultimap
import org.junit.Assert.assertArrayEquals
import org.junit.Test

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class InternationalizedPropertiesTest {

    /** Resource names without extensions. */
    val resources = listOf(
            "some",
            "some_other",
            "strings_en",
            "strings_zh",
            "strings_in",
            "strings_strings",
            "other_en",
            "other_in"
    )

    @Test
    fun test() {
        val resourceBundles = resources.filterInternationalizedProperties()
        val multimap = LinkedHashMultimap.create<String, String>()
        resourceBundles.distinctInternationalizedPropertiesIdentifier().forEach { key ->
            multimap.putAll(key, resourceBundles.filter { it.startsWith(key) })
        }
        assertArrayEquals(
                multimap.keySet().toTypedArray(),
                arrayOf("strings", "other"))
        assertArrayEquals(
                multimap.values().toTypedArray(),
                arrayOf("strings_en", "strings_zh", "strings_in", "other_en", "other_in"))
    }
}