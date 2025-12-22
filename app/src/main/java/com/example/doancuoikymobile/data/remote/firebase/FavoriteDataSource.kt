package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.FavoriteSong
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoriteDataSource {
    private val db = FirebaseFirestore.getInstance()
    private val favoritesRef = db.collection("favorite_songs")

    suspend fun addFavorite(userId: String, songId: String) {
        val fav = FavoriteSong(userId, songId)
        favoritesRef.document("$userId-$songId").set(fav).await()
    }

    suspend fun removeFavorite(userId: String, songId: String) {
        favoritesRef.document("$userId-$songId").delete().await()
    }

    suspend fun isFavorite(userId: String, songId: String): Boolean {
        return try {
            favoritesRef.document("$userId-$songId").get().await().exists()
        } catch (e: Exception) { false }
    }
}