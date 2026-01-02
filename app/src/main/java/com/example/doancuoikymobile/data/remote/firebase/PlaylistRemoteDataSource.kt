package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.Playlist
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import android.util.Log

class PlaylistRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val coll = firestore.collection("playlists")

    suspend fun getPlaylistOnce(id: String): Playlist? {
        val doc = coll.document(id).get().await()
        return doc.toObject(Playlist::class.java)?.copy(playlistId = doc.id)
    }

    fun watchUserPlaylists(userId: String): Flow<List<Playlist>> = callbackFlow {
    val registration = coll.whereEqualTo("userId", userId)
        .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
        .addSnapshotListener { snap, error ->
            if (error != null) {
                Log.e("PlaylistRemote", "watchUserPlaylists error", error)

                trySend(emptyList()).isSuccess
                return@addSnapshotListener
            }

            val list = snap?.documents
                ?.mapNotNull {
                    it.toObject(Playlist::class.java)
                        ?.copy(playlistId = it.id)
                }
                ?: emptyList()

            trySend(list).isSuccess
        }

    awaitClose { registration.remove() }
}

    suspend fun upsertPlaylist(playlist: Playlist) {
        coll.document(playlist.playlistId).set(playlist).await()
    }

    suspend fun deletePlaylist(id: String) {
        coll.document(id).delete().await()
    }

    suspend fun getUserPlaylistsOnce(userId: String): List<Playlist> {
        val snapshot = coll.whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull {
            it.toObject(Playlist::class.java)?.copy(playlistId = it.id)
        }
    }
}
