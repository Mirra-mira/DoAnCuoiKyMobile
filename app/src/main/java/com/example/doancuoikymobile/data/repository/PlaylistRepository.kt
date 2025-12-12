package com.example.doancuoikymobile.data.repository

import com.example.doancuoikymobile.data.firebase.PlaylistRemoteDataSource
import com.example.doancuoikymobile.data.model.Playlist
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(
    private val remote: PlaylistRemoteDataSource
) {
    suspend fun getPlaylistOnce(id: String): Playlist? = remote.getPlaylistOnce(id)
    fun watchUserPlaylists(userId: String): Flow<List<Playlist>> = remote.watchUserPlaylists(userId)
    suspend fun upsertPlaylist(playlist: Playlist) = remote.upsertPlaylist(playlist)
    suspend fun deletePlaylist(id: String) = remote.deletePlaylist(id)
}
