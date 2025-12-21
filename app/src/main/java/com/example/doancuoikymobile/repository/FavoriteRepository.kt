package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.FavoriteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FavoriteRepository {
    private val favoriteDataSource = FavoriteDataSource()

    suspend fun addToFavorite(userId: String, songId: String) {
        favoriteDataSource.addFavorite(userId, songId)
    }

    suspend fun removeFromFavorite(userId: String, songId: String) {
        favoriteDataSource.removeFavorite(userId, songId)
    }

    suspend fun isFavorite(userId: String, songId: String): Boolean {
        return favoriteDataSource.isFavorite(userId, songId)
    }

    fun getFavoriteSongs(userId: String): Flow<List<String>> = flow {
        emit(emptyList())
    }
}