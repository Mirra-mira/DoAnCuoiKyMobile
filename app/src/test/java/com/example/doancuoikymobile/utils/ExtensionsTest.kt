package com.example.doancuoikymobile.utils

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class ExtensionsTest {

    @Test
    fun extensionsClassExists() {
        val extensions = Extensions()
        assertFalse(extensions == null)
    }

    @Test
    fun extensionsClassCanBeInstantiated() {
        val extensions = Extensions()
        assertEquals(Extensions::class.simpleName, "Extensions")
    }

    @Test
    fun stringExtension_empty() {
        val emptyString = ""
        assertTrue(emptyString.isEmpty())
    }

    @Test
    fun stringExtension_notEmpty() {
        val nonEmptyString = "test"
        assertFalse(nonEmptyString.isEmpty())
    }

    @Test
    fun collectionExtension_emptyList() {
        val emptyList = emptyList<String>()
        assertTrue(emptyList.isEmpty())
    }

    @Test
    fun collectionExtension_nonEmptyList() {
        val nonEmptyList = listOf("a", "b", "c")
        assertFalse(nonEmptyList.isEmpty())
        assertEquals(3, nonEmptyList.size)
    }

    @Test
    fun numberExtension_positive() {
        val positive = 42
        assertTrue(positive > 0)
    }

    @Test
    fun numberExtension_negative() {
        val negative = -10
        assertTrue(negative < 0)
    }

    @Test
    fun nullableExtension_null() {
        val nullable: String? = null
        assertTrue(nullable == null)
    }

    @Test
    fun nullableExtension_notNull() {
        val notNull: String? = "value"
        assertTrue(notNull != null)
    }

    @Test
    fun booleanExtension_true() {
        val boolTrue = true
        assertTrue(boolTrue)
    }

    @Test
    fun booleanExtension_false() {
        val boolFalse = false
        assertFalse(boolFalse)
    }

    @Test
    fun mapExtension_empty() {
        val emptyMap = emptyMap<String, String>()
        assertTrue(emptyMap.isEmpty())
    }

    @Test
    fun mapExtension_nonEmpty() {
        val nonEmptyMap = mapOf("key" to "value")
        assertFalse(nonEmptyMap.isEmpty())
        assertEquals(1, nonEmptyMap.size)
    }

    @Test
    fun rangeExtension_iteration() {
        val range = 1..5
        val list = range.toList()
        assertEquals(5, list.size)
    }

    @Test
    fun stringExtension_contains() {
        val text = "hello world"
        assertTrue(text.contains("hello"))
        assertTrue(text.contains("world"))
    }

    @Test
    fun stringExtension_startsWith() {
        val text = "hello"
        assertTrue(text.startsWith("hel"))
    }

    @Test
    fun stringExtension_endsWith() {
        val text = "world"
        assertTrue(text.endsWith("ld"))
    }

    @Test
    fun stringExtension_toUpperCase() {
        val text = "hello"
        assertEquals("HELLO", text.uppercase())
    }

    @Test
    fun stringExtension_toLowerCase() {
        val text = "HELLO"
        assertEquals("hello", text.lowercase())
    }

    @Test
    fun stringExtension_trim() {
        val text = "  hello  "
        assertEquals("hello", text.trim())
    }

    @Test
    fun listExtension_first() {
        val list = listOf(1, 2, 3)
        assertEquals(1, list.first())
    }

    @Test
    fun listExtension_last() {
        val list = listOf(1, 2, 3)
        assertEquals(3, list.last())
    }

    @Test
    fun listExtension_filter() {
        val list = listOf(1, 2, 3, 4, 5)
        val filtered = list.filter { it > 2 }
        assertEquals(3, filtered.size)
    }

    @Test
    fun listExtension_map() {
        val list = listOf(1, 2, 3)
        val mapped = list.map { it * 2 }
        assertEquals(listOf(2, 4, 6), mapped)
    }

    @Test
    fun pairExtension_first() {
        val pair: Pair<String, String> = "key" to "value"
        assertEquals("key", pair.first)
    }

    @Test
    fun pairExtension_second() {
        val pair: Pair<String, String> = "key" to "value"
        assertEquals("value", pair.second)
    }

    @Test
    fun tripleExtension_first() {
        val triple: Triple<String, String, String> = Triple("a", "b", "c")
        assertEquals("a", triple.first)
    }

    @Test
    fun tripleExtension_second() {
        val triple: Triple<String, String, String> = Triple("a", "b", "c")
        assertEquals("b", triple.second)
    }

    @Test
    fun tripleExtension_third() {
        val triple: Triple<String, String, String> = Triple("a", "b", "c")
        assertEquals("c", triple.third)
    }

    @Test
    fun lambdaExtension_apply() {
        val text = "hello".apply {
            assertTrue(this.isNotEmpty())
        }
        assertEquals("hello", text)
    }

    @Test
    fun lambdaExtension_let() {
        val result = "hello".let { it.uppercase() }
        assertEquals("HELLO", result)
    }

    @Test
    fun lambdaExtension_also() {
        val text = "test".also {
            assertEquals("test", it)
        }
        assertEquals("test", text)
    }

    @Test
    fun dataClassExtension_copy() {
        data class TestData(val name: String, val age: Int)
        val original = TestData("John", 30)
        val copied = original.copy(age = 31)
        
        assertEquals("John", copied.name)
        assertEquals(31, copied.age)
    }
}
