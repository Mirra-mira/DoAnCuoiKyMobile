package com.example.doancuoikymobile.data.remote.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.example.doancuoikymobile.model.Artist

/**
 * Deezer Artist Data Source
 * Handles artist searches and details from Deezer API
 */
class DeezerArtistDataSource(
    private val deezerApi: DeezerApiService = DeezerRetrofitClient.deezerApiService
) {

    /**
     * Search artists by query using Deezer API
     */
    fun searchArtists(query: String): Flow<List<Artist>> = flow {
        try {
            if (query.isBlank()) {
                emit(emptyList())
                return@flow
            }

            val response = deezerApi.searchArtists(query, limit = 50)
            val artists = response.data.map { deezerArtist ->
                Artist(
                    artistId = deezerArtist.id.toString(),
                    name = deezerArtist.name,
                    pictureUrl = deezerArtist.picture_big ?: deezerArtist.picture,
                    followers = deezerArtist.nb_fan
                )
            }
            emit(artists)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    /**
     * Get artist details from Deezer API
     */
    suspend fun getArtist(artistId: Long): Artist? {
        return try {
            val response = deezerApi.getArtist(artistId)
            Artist(
                artistId = response.id.toString(),
                name = response.name,
                pictureUrl = response.picture_big ?: response.picture,
                followers = response.nb_fan
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
