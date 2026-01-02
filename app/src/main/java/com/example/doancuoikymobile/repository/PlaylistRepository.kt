package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.PlaylistRemoteDataSource
import com.example.doancuoikymobile.data.remote.firebase.PlaylistSongDataSource
import com.example.doancuoikymobile.model.Playlist
import com.example.doancuoikymobile.model.PlaylistSong
import kotlinx.coroutines.flow.Flow

/**
 * Repository for playlist data.
 * Orchestrates:
 * - PlaylistRemoteDataSource (Firestore playlist metadata)
 * - PlaylistSongDataSource (Firestore many-to-many relationships)
 *
 * Responsibilities:
 * - Create/read/update/delete playlists
 * - Add/remove songs from playlists
 * - Reorder songs in playlists
 * - Watch playlist composition changes
 */
class PlaylistRepository(
    private val playlistRemote: PlaylistRemoteDataSource,
    private val playlistSongRemote: PlaylistSongDataSource
) {
    
    /**
     * Get a single playlist by ID.
     */
    suspend fun getPlaylistOnce(id: String): Playlist? = playlistRemote.getPlaylistOnce(id)

    /**
     * Stream all playlists for a user (real-time, ordered by createdAt descending).
     */
    fun watchUserPlaylists(userId: String): Flow<List<Playlist>> =
        playlistRemote.watchUserPlaylists(userId)

    /**
     * Create or update a playlist.
     */
    suspend fun upsertPlaylist(playlist: Playlist) = playlistRemote.upsertPlaylist(playlist)

    /**
     * Delete a playlist and all its songs.
     */
    suspend fun deletePlaylist(id: String) = playlistRemote.deletePlaylist(id)

    /**
     * Add a song to a playlist.
     * @param playlistId The playlist to add to
     * @param songId The song to add
     * @param orderIndex The position in the playlist (default 0)
     */
    suspend fun addSongToPlaylist(playlistId: String, songId: String, orderIndex: Int = 0) =
        playlistSongRemote.addSongToPlaylist(playlistId, songId, orderIndex)

    /**
     * Remove a song from a playlist.
     */
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) =
        playlistSongRemote.removeSongFromPlaylist(playlistId, songId)

    /**
     * Stream all songs in a playlist (real-time, ordered).
     */
    fun watchPlaylistSongs(playlistId: String): Flow<List<PlaylistSong>> =
        playlistSongRemote.watchPlaylistSongs(playlistId)

    /**
     * Reorder a song in a playlist.
     */
    suspend fun updateSongOrderInPlaylist(playlistId: String, songId: String, newOrderIndex: Int) =
        playlistSongRemote.updateSongOrderInPlaylist(playlistId, songId, newOrderIndex)

    /**
     * Check if a song is in a playlist.
     */
    suspend fun isSongInPlaylist(playlistId: String, songId: String): Boolean =
        playlistSongRemote.isSongInPlaylist(playlistId, songId)

    suspend fun getUserPlaylists(): List<Playlist> {
        val userId = "CURRENT_USER_ID"  // TODO: replace bằng userId thật
        return playlistRemote.getUserPlaylistsOnce(userId)
    }
}

