package com.example.doancuoikymobile.data.repository

import com.example.doancuoikymobile.data.firebase.ArtistRemoteDataSource
import com.example.doancuoikymobile.data.model.Artist
import kotlinx.coroutines.flow.Flow

class ArtistRepository(
    private val remote: ArtistRemoteDataSource
) {
    suspend fun getArtistOnce(id: String): Artist? = remote.getArtistOnce(id)
    fun watchAll(): Flow<List<Artist>> = remote.watchAll()
    suspend fun upsertArtist(artist: Artist) = remote.upsertArtist(artist)
}
