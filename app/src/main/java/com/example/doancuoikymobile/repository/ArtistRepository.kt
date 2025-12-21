package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.ArtistRemoteDataSource
import com.example.doancuoikymobile.model.Artist
import com.example.doancuoikymobile.utils.SearchKeywordGenerator
import kotlinx.coroutines.flow.Flow

/**
 * Repository for artist data.
 * Provides access to artist metadata stored in Firestore.
 * 
 * Responsibilities:
 * - Fetch artist metadata from Firestore
 * - Search artists by name
 * - Generate searchKeywords for Firestore search
 */
class ArtistRepository(
    private val remote: ArtistRemoteDataSource
) {
    
    /**
     * Get a single artist by ID.
     */
    suspend fun getArtistOnce(id: String): Artist? = remote.getArtistOnce(id)

    /**
     * Stream all artists from Firestore.
     */
    fun watchAll(): Flow<List<Artist>> = remote.watchAll()

    /**
     * Search artists by name (Firestore search with keywords).
     */
    fun searchArtists(query: String): Flow<List<Artist>> = remote.searchArtists(query)

    /**
     * Create or update an artist in Firestore.
     * Automatically generates searchKeywords from name if not provided.
     */
    suspend fun upsertArtist(artist: Artist) {
        val updated = if (artist.searchKeywords.isEmpty()) {
            artist.copy(searchKeywords = SearchKeywordGenerator.generateKeywords(artist.name))
        } else {
            artist
        }
        remote.upsertArtist(updated)
    }

    /**
     * Delete an artist from Firestore.
     */
    suspend fun deleteArtist(id: String) = remote.deleteArtist(id)
}
