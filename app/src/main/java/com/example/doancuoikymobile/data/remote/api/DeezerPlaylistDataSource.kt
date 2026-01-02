package com.example.doancuoikymobile.data.remote.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.example.doancuoikymobile.model.Playlist

/**
 * Deezer Playlist Data Source
 * Handles playlist searches from Deezer API
 */
class DeezerPlaylistDataSource(
    private val deezerApi: DeezerApiService = DeezerRetrofitClient.deezerApiService
) {

    /**
     * Search playlists by query using Deezer API
     */
    fun searchPlaylists(query: String): Flow<List<Playlist>> = flow {
        try {
            if (query.isBlank()) {
                emit(emptyList())
                return@flow
            }

            val response = deezerApi.searchPlaylists(query, limit = 50)
            val playlists = response.data.map { deezerPlaylist ->
                Playlist(
                    playlistId = deezerPlaylist.id.toString(),
                    userId = "", // Deezer playlists are public, no specific user
                    name = deezerPlaylist.title,
                    createdAt = System.currentTimeMillis() // Use current time as placeholder
                )
            }
            emit(playlists)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }
}

