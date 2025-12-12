package com.example.doancuoikymobile.data.firebase

import com.example.doancuoikymobile.data.model.FavoriteSong
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose

class FavoriteRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val coll = firestore.collection("favorites")

    suspend fun addFavorite(fav: FavoriteSong) {
        // store as document id = "${fav.userId}_${fav.songId}"
        val docId = "${fav.userId}_${fav.songId}"
        coll.document(docId).set(fav).await()
    }

    suspend fun removeFavorite(userId: String, songId: String) {
        coll.document("${userId}_$songId").delete().await()
    }

    fun watchFavorites(userId: String): Flow<List<FavoriteSong>> = callbackFlow {
        val registration = coll.whereEqualTo("userId", userId)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toObject(FavoriteSong::class.java) } ?: emptyList()
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }
}
