package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.PlaylistSong
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose

/**
 * Manages the many-to-many relationship between playlists and songs.
 * - Stores PlaylistSong documents (playlistId, songId, orderIndex)
 * - Does NOT manage the playlist or song metadata (delegate to PlaylistRemoteDataSource/SongRemoteDataSource)
 */
class PlaylistSongDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val coll = firestore.collection("playlist_songs")

    /**
     * Add a song to a playlist.
     */
    suspend fun addSongToPlaylist(playlistId: String, songId: String, orderIndex: Int = 0) {
        val docId = "${playlistId}_$songId"
        val ps = PlaylistSong(playlistId = playlistId, songId = songId, orderIndex = orderIndex)
        coll.document(docId).set(ps).await()
    }

    /**
     * Remove a song from a playlist.
     */
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        val docId = "${playlistId}_$songId"
        coll.document(docId).delete().await()
    }

    /**
     * Get all songs in a playlist (real-time stream, ordered by orderIndex).
     */
    fun watchPlaylistSongs(playlistId: String): Flow<List<PlaylistSong>> = callbackFlow {
        val registration = coll.whereEqualTo("playlistId", playlistId)
            .orderBy("orderIndex", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull {
                    it.toObject(PlaylistSong::class.java)
                } ?: emptyList()
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }

    /**
     * Update the order index of a song in a playlist (for reordering).
     */
    suspend fun updateSongOrderInPlaylist(playlistId: String, songId: String, newOrderIndex: Int) {
        val docId = "${playlistId}_$songId"
        coll.document(docId).update("orderIndex", newOrderIndex).await()
    }

    /**
     * Check if a song exists in a playlist.
     */
    suspend fun isSongInPlaylist(playlistId: String, songId: String): Boolean {
        val docId = "${playlistId}_$songId"
        val doc = coll.document(docId).get().await()
        return doc.exists()
    }
}
