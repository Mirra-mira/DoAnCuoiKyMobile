package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.Song
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose

class SongRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val songsColl = firestore.collection("songs")

    suspend fun getSongOnce(songId: String): Song? {
        val doc = songsColl.document(songId).get().await()
        return if (doc.exists()) {
            doc.toObject(Song::class.java)?.copy(songId = doc.id)
        } else {
            null
        }
    }

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

    suspend fun saveSong(song: Song): Boolean {
        return try {
            songsColl.document(song.songId).set(song).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveSongIfNotExists(song: Song): Boolean {
        return try {
            val existingDoc = songsColl.document(song.songId).get().await()
            if (!existingDoc.exists()) {
                songsColl.document(song.songId).set(song).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteSong(songId: String): Boolean {
        return try {
            songsColl.document(songId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun searchSongsOnce(query: String): List<Song> {
        return try {
            val snapshot = songsColl
                .whereArrayContains("searchKeywords", query.lowercase())
                .get().await()
            val firebaseSongs = snapshot.documents.mapNotNull {
                it.toObject(Song::class.java)?.copy(songId = it.id)
            }
            firebaseSongs
        } catch (e: Exception) {
            emptyList()
        }
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

    suspend fun upsertSong(song: Song) {
        songsColl.document(song.songId).set(song).await()
    }
}
