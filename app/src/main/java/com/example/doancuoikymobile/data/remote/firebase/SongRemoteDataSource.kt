package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.Song
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose

/**
 * UPDATED FILE: SongRemoteDataSource.kt
 *
 * Manages song metadata stored in Firestore.
 * - Reads/writes song documents (title, duration, audioUrl, coverUrl, mainArtistId, searchKeywords)
 * - audioUrl points to a Firebase Storage signed URL
 * - searchKeywords enables Firestore search queries
 * - Does NOT handle file uploads (delegate to StorageDataSource)
 */
class SongRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val songsColl = firestore.collection("songs")

    /**
     * Fetch a single song by ID from Firestore.
     */
    suspend fun getSongOnce(songId: String): Song? {
        val doc = songsColl.document(songId).get().await()
        return if (doc.exists()) {
            doc.toObject(Song::class.java)?.copy(songId = doc.id)
        } else {
            null
        }
    }

    /**
     * Real-time stream of all songs in Firestore.
     * Useful for browsing library, search results, etc.
     */
    fun watchAllSongs(): Flow<List<Song>> = callbackFlow {
        val registration = songsColl.addSnapshotListener { snap, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull {
                it.toObject(Song::class.java)?.copy(songId = it.id)
            } ?: emptyList()
            trySend(list).isSuccess
        }
        awaitClose { registration.remove() }
    }

    /**
     * Search songs by query using searchKeywords array.
     * Firestore query: whereArrayContains("searchKeywords", query.lowercase())
     */
    fun searchSongs(query: String): Flow<List<Song>> = callbackFlow {
        val registration = songsColl
            .whereArrayContains("searchKeywords", query.lowercase())
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Song::class.java)?.copy(songId = it.id)
                } ?: emptyList()
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }

    /**
     * Create or update a song in Firestore.
     * Caller must ensure:
     * - audioUrl is already set (obtained from StorageDataSource after upload)
     * - searchKeywords is populated (use SearchKeywordGenerator)
     */
    suspend fun upsertSong(song: Song) {
        songsColl.document(song.songId).set(song).await()
    }

    /**
     * Delete a song metadata from Firestore.
     * Note: Caller must separately delete the MP3 file from Firebase Storage.
     */
    suspend fun deleteSong(songId: String) {
        songsColl.document(songId).delete().await()
    }

    /**
     * Fetch songs by a specific artist (real-time stream).
     */
    fun watchSongsByArtist(artistId: String): Flow<List<Song>> = callbackFlow {
        val registration = songsColl.whereEqualTo("mainArtistId", artistId)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Song::class.java)?.copy(songId = it.id)
                } ?: emptyList()
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }
}
