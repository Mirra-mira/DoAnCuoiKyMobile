package com.example.doancuoikymobile.data.repository

import com.example.doancuoikymobile.data.firebase.FavoriteRemoteDataSource
import com.example.doancuoikymobile.data.model.FavoriteSong
import kotlinx.coroutines.flow.Flow

class FavoriteRepository(
    private val remote: FavoriteRemoteDataSource
) {
    suspend fun addFavorite(fav: FavoriteSong) = remote.addFavorite(fav)
    suspend fun removeFavorite(userId: String, songId: String) = remote.removeFavorite(userId, songId)
    fun watchFavorites(userId: String): Flow<List<FavoriteSong>> = remote.watchFavorites(userId)
}
