package com.example.doancuoikymobile.data.firebase

import com.example.doancuoikymobile.data.model.Artist
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ArtistRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val artistsColl = firestore.collection("artists")

    suspend fun getArtistOnce(artistId: String): Artist? {
        val doc = artistsColl.document(artistId).get().await()
        return doc.toObject(Artist::class.java)?.copy(artistId = doc.id)
    }

    fun watchAll(): Flow<List<Artist>> = callbackFlow {
        val registration = artistsColl.addSnapshotListener { snap, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val list = snap?.documents?.mapNotNull { it.toObject(Artist::class.java)?.copy(artistId = it.id) } ?: emptyList()
            trySend(list).isSuccess
        }
        awaitClose { registration.remove() }
    }

    suspend fun upsertArtist(artist: Artist) {
        artistsColl.document(artist.artistId).set(artist).await()
    }
}
