package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.Artist
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * UPDATED FILE: ArtistRemoteDataSource.kt
 *
 * Manages artist metadata stored in Firestore.
 * - Reads/writes artist documents (name, pictureUrl, searchKeywords)
 * - pictureUrl points to a Firebase Storage URL or external CDN
 * - searchKeywords enables Firestore search queries
 * - Does NOT handle picture uploads (delegate to StorageDataSource if needed)
 */
class ArtistRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val artistsColl = firestore.collection("artists")

    /**
     * Fetch a single artist by ID from Firestore.
     */
    suspend fun getArtistOnce(artistId: String): Artist? {
        val doc = artistsColl.document(artistId).get().await()
        return if (doc.exists()) {
            doc.toObject(Artist::class.java)?.copy(artistId = doc.id)
        } else {
            null
        }
    }

    /**
     * Real-time stream of all artists in Firestore.
     */
    fun watchAll(): Flow<List<Artist>> = callbackFlow {
        val registration = artistsColl.addSnapshotListener { snap, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull {
                it.toObject(Artist::class.java)?.copy(artistId = it.id)
            } ?: emptyList()
            trySend(list).isSuccess
        }
        awaitClose { registration.remove() }
    }

    /**
     * Search artists by name using searchKeywords array.
     * Firestore query: whereArrayContains("searchKeywords", query.lowercase())
     */
    fun searchArtists(query: String): Flow<List<Artist>> = callbackFlow {
        val registration = artistsColl
            .whereArrayContains("searchKeywords", query.lowercase())
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Artist::class.java)?.copy(artistId = it.id)
                } ?: emptyList()
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }

    /**
     * Create or update an artist in Firestore.
     * Caller must ensure searchKeywords is populated (use SearchKeywordGenerator).
     */
    suspend fun upsertArtist(artist: Artist) {
        artistsColl.document(artist.artistId).set(artist).await()
    }

    /**
     * Delete an artist from Firestore.
     */
    suspend fun deleteArtist(artistId: String) {
        artistsColl.document(artistId).delete().await()
    }
}
