package com.example.doancuoikymobile.utils

/**
 * Utility for generating search keywords for Firestore queries.
 * Generates lowercase variants of text for efficient full-text search.
 */
object SearchKeywordGenerator {
    
    /**
     * Generate searchable keywords from text.
     * Creates lowercase words and trigrams for flexible search.
     * 
     * Example: "Hello World" -> ["hello", "world", "hel", "ell", "llo", "wor", "orl", "rld"]
     */
    fun generateKeywords(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        
        val keywords = mutableSetOf<String>()
        val normalized = text.lowercase().trim()
        
        // Add whole words
        val words = normalized.split(Regex("\\s+|[^a-z0-9]+"))
            .filter { it.isNotEmpty() }
        keywords.addAll(words)
        
        // Add trigrams for each word (for partial matching)
        for (word in words) {
            if (word.length >= 3) {
                for (i in 0..word.length - 3) {
                    keywords.add(word.substring(i, i + 3))
                }
            }
        }
        
        return keywords.toList()
    }

    /**
     * Generate keywords from both title and subtitle (e.g., song title and artist name).
     */
    fun generateKeywords(title: String, subtitle: String?): List<String> {
        val keywords = mutableSetOf<String>()
        keywords.addAll(generateKeywords(title))
        if (!subtitle.isNullOrEmpty()) {
            keywords.addAll(generateKeywords(subtitle))
        }
        return keywords.toList()
    }
}
