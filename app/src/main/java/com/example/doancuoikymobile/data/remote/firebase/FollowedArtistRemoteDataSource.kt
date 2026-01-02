package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.FollowedArtist
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FollowedArtistRemoteDataSource(private val firestore: FirebaseFirestore) {

    /**
     * Watch user's followed artists in real-time
     */
    fun watchUserFollowedArtists(userId: String): Flow<List<FollowedArtist>> = callbackFlow {
        val query = firestore.collection("users")
            .document(userId)
            .collection("followed_artists")
        
        val listener = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }
            
            val followedArtists = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(FollowedArtist::class.java)
            } ?: emptyList()
            
            trySend(followedArtists).isSuccess
        }
        
        awaitClose {
            listener.remove()
        }
    }

    /**
     * Follow an artist
     */
    suspend fun followArtist(userId: String, artistId: String): Boolean {
        return try {
            val followId = UUID.randomUUID().toString()
            val followedArtist = FollowedArtist(
                followId = followId,
                userId = userId,
                artistId = artistId,
                followedAt = System.currentTimeMillis()
            )
            
            firestore.collection("users")
                .document(userId)
                .collection("followed_artists")
                .document(followId)
                .set(followedArtist)
                .await()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Unfollow an artist
     */
    suspend fun unfollowArtist(userId: String, artistId: String): Boolean {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("followed_artists")
                .whereEqualTo("artistId", artistId)
                .get()
                .await()
                .documents
                .forEach { doc ->
                    doc.reference.delete().await()
                }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Check if user follows an artist
     */
    suspend fun isFollowedByUser(userId: String, artistId: String): Boolean {
        return try {
            val result = firestore.collection("users")
                .document(userId)
                .collection("followed_artists")
                .whereEqualTo("artistId", artistId)
                .get()
                .await()
            
            result.documents.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get all followed artist IDs for a user
     */
    suspend fun getFollowedArtistIds(userId: String): List<String> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("followed_artists")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(FollowedArtist::class.java)?.artistId }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
