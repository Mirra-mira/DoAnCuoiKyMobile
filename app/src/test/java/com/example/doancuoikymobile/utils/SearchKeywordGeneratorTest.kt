package com.example.doancuoikymobile.utils

import org.junit.Test
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue

class SearchKeywordGeneratorTest {

    @Test
    fun generateKeywords_simpleName_returnsKeywords() {
        val keywords = SearchKeywordGenerator.generateKeywords("hello")

        assertEquals(1, keywords.count { it == "hello" })
        assertTrue(keywords.contains("hel"))
        assertTrue(keywords.contains("ell"))
        assertTrue(keywords.contains("llo"))
    }

    @Test
    fun generateKeywords_empty_returnsEmpty() {
        val keywords = SearchKeywordGenerator.generateKeywords("")

        assertEquals(0, keywords.size)
    }

    @Test
    fun generateKeywords_multipleWords_returnsKeywords() {
        val keywords = SearchKeywordGenerator.generateKeywords("hello world")

        assertTrue(keywords.contains("hello"))
        assertTrue(keywords.contains("world"))
    }

    @Test
    fun generateKeywords_specialCharacters_handlesCorrectly() {
        val keywords = SearchKeywordGenerator.generateKeywords("hello-world_test")

        assertTrue(keywords.isNotEmpty())
        assertTrue(keywords.any { it.contains("hello") })
    }

    @Test
    fun generateKeywords_uppercase_normalized() {
        val keywordsLower = SearchKeywordGenerator.generateKeywords("hello")
        val keywordsUpper = SearchKeywordGenerator.generateKeywords("HELLO")

        assertEquals(keywordsLower.size, keywordsUpper.size)
    }

    @Test
    fun generateKeywords_withWhitespace_trimmed() {
        val keywords = SearchKeywordGenerator.generateKeywords("  hello  world  ")

        assertTrue(keywords.contains("hello"))
        assertTrue(keywords.contains("world"))
    }

    @Test
    fun generateKeywords_shortWord_noTrigrams() {
        val keywords = SearchKeywordGenerator.generateKeywords("hi")

        assertEquals(1, keywords.size)
        assertEquals("hi", keywords[0])
    }

    @Test
    fun generateKeywords_threeCharWord_hasTrigramOnly() {
        val keywords = SearchKeywordGenerator.generateKeywords("abc")

        assertTrue(keywords.contains("abc"))
        assertEquals(1, keywords.size)
    }

    @Test
    fun generateKeywords_fourCharWord_hasTrigrams() {
        val keywords = SearchKeywordGenerator.generateKeywords("test")

        assertTrue(keywords.contains("test"))
        assertTrue(keywords.contains("tes"))
        assertTrue(keywords.contains("est"))
    }

    @Test
    fun generateKeywords_titleAndSubtitle_returnsBoth() {
        val keywords = SearchKeywordGenerator.generateKeywords("song title", "artist name")

        assertTrue(keywords.contains("song"))
        assertTrue(keywords.contains("title"))
        assertTrue(keywords.contains("artist"))
        assertTrue(keywords.contains("name"))
    }

    @Test
    fun generateKeywords_titleAndNullSubtitle_returnsTitleOnly() {
        val keywords = SearchKeywordGenerator.generateKeywords("song title", null)

        assertTrue(keywords.contains("song"))
        assertTrue(keywords.contains("title"))
    }

    @Test
    fun generateKeywords_titleAndEmptySubtitle_returnsTitleOnly() {
        val keywords = SearchKeywordGenerator.generateKeywords("song title", "")

        assertTrue(keywords.contains("song"))
        assertTrue(keywords.contains("title"))
    }

    @Test
    fun generateKeywords_noUniqueDuplicates() {
        val keywords = SearchKeywordGenerator.generateKeywords("hello hello")

        val uniqueKeywords = keywords.toSet()
        assertEquals(uniqueKeywords.size, keywords.size)
    }

    @Test
    fun generateKeywords_longWord_allTrigramsGenerated() {
        val keywords = SearchKeywordGenerator.generateKeywords("excellent")

        assertTrue(keywords.contains("exc"))
        assertTrue(keywords.contains("xce"))
        assertTrue(keywords.contains("cel"))
        assertTrue(keywords.contains("ent"))
    }

    @Test
    fun generateKeywords_numbersIncluded() {
        val keywords = SearchKeywordGenerator.generateKeywords("test123")

        assertTrue(keywords.isNotEmpty())
        assertTrue(keywords.any { it.contains("test") })
    }

    @Test
    fun generateKeywords_onlyNumbers_returns() {
        val keywords = SearchKeywordGenerator.generateKeywords("123")

        assertTrue(keywords.contains("123"))
    }

    @Test
    fun generateKeywords_mixedCase_normalized() {
        val keywords = SearchKeywordGenerator.generateKeywords("TeStInG")

        assertTrue(keywords.any { it == "testing" })
    }

    @Test
    fun generateKeywords_singleCharacter_returnsEmpty() {
        val keywords = SearchKeywordGenerator.generateKeywords("a")

        assertEquals(1, keywords.size)
        assertEquals("a", keywords[0])
    }

    @Test
    fun generateKeywords_newlineAndTabCharacters() {
        val keywords = SearchKeywordGenerator.generateKeywords("hello\nworld\ttest")

        assertTrue(keywords.contains("hello"))
        assertTrue(keywords.contains("world"))
        assertTrue(keywords.contains("test"))
    }

    @Test
    fun generateKeywords_verifyAllReturned() {
        val keywords = SearchKeywordGenerator.generateKeywords("cat")

        assertTrue(keywords.contains("cat"))
    }

    @Test
    fun generateKeywords_notEmpty() {
        val keywords = SearchKeywordGenerator.generateKeywords("music")

        assertFalse(keywords.isEmpty())
    }

    @Test
    fun generateKeywords_artistNameFormat() {
        val keywords = SearchKeywordGenerator.generateKeywords("The Beatles", "John Lennon")

        assertTrue(keywords.any { it.contains("beatles") || it.contains("the") })
        assertTrue(keywords.any { it.contains("john") || it.contains("lennon") })
    }

    @Test
    fun generateKeywords_songTitleFormat() {
        val keywords = SearchKeywordGenerator.generateKeywords("Let It Be", "The Beatles")

        assertTrue(keywords.contains("let") || keywords.contains("it") || keywords.contains("be"))
        assertTrue(keywords.contains("beatles") || keywords.contains("the"))
    }
}
