package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.LikedSong
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class LikedSongRemoteDataSource(private val firestore: FirebaseFirestore) {

    /**
     * Watch user's liked songs in real-time
     */
    fun watchUserLikedSongs(userId: String): Flow<List<LikedSong>> = callbackFlow {
        val query = firestore.collection("users")
            .document(userId)
            .collection("liked_songs")
        
        val listener = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }
            
            val likedSongs = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(LikedSong::class.java)
            } ?: emptyList()
            
            trySend(likedSongs).isSuccess
        }
        
        awaitClose {
            listener.remove()
        }
    }

    /**
     * Add a song to user's liked list
     */
    suspend fun addLikedSong(userId: String, songId: String): Boolean {
        return try {
            val likeId = UUID.randomUUID().toString()
            val likedSong = LikedSong(
                likeId = likeId,
                userId = userId,
                songId = songId,
                likedAt = System.currentTimeMillis()
            )
            
            firestore.collection("users")
                .document(userId)
                .collection("liked_songs")
                .document(likeId)
                .set(likedSong)
                .await()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Remove a song from user's liked list
     */
    suspend fun removeLikedSong(userId: String, songId: String): Boolean {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("liked_songs")
                .whereEqualTo("songId", songId)
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
     * Check if a song is liked by user
     */
    suspend fun isLikedBySong(userId: String, songId: String): Boolean {
        return try {
            val result = firestore.collection("users")
                .document(userId)
                .collection("liked_songs")
                .whereEqualTo("songId", songId)
                .get()
                .await()
            
            result.documents.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get all liked song IDs for a user
     */
    suspend fun getLikedSongIds(userId: String): List<String> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("liked_songs")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(LikedSong::class.java)?.songId }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
