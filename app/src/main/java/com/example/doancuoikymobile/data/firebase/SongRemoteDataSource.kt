package com.example.doancuoikymobile.data.firebase

import com.example.doancuoikymobile.data.model.Song
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
        return doc.toObject(Song::class.java)?.copy(songId = doc.id)
    }

    fun watchAllSongs(): Flow<List<Song>> = callbackFlow {
        val registration = songsColl.addSnapshotListener { snap, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull { it.toObject(Song::class.java)?.copy(songId = it.id) } ?: emptyList()
            trySend(list).isSuccess
        }
        awaitClose { registration.remove() }
    }

    suspend fun upsertSong(song: Song) {
        songsColl.document(song.songId).set(song).await()
    }

    suspend fun deleteSong(songId: String) {
        songsColl.document(songId).delete().await()
    }
}
