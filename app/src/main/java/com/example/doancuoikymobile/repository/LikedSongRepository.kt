package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.LikedSongRemoteDataSource
import com.example.doancuoikymobile.model.LikedSong
import kotlinx.coroutines.flow.Flow

class LikedSongRepository(private val remote: LikedSongRemoteDataSource) {

    /**
     * Watch user's liked songs in real-time
     */
    fun watchUserLikedSongs(userId: String): Flow<List<LikedSong>> {
        return remote.watchUserLikedSongs(userId)
    }

    /**
     * Toggle like status for a song
     */
    suspend fun toggleLikeSong(userId: String, songId: String, isCurrentlyLiked: Boolean): Boolean {
        return if (isCurrentlyLiked) {
            remote.removeLikedSong(userId, songId)
        } else {
            remote.addLikedSong(userId, songId)
        }
    }

    /**
     * Check if a song is liked
     */
    suspend fun isLiked(userId: String, songId: String): Boolean {
        return remote.isLikedBySong(userId, songId)
    }

    /**
     * Like a song
     */
    suspend fun likeSong(userId: String, songId: String): Boolean {
        return remote.addLikedSong(userId, songId)
    }

    /**
     * Unlike a song
     */
    suspend fun unlikeSong(userId: String, songId: String): Boolean {
        return remote.removeLikedSong(userId, songId)
    }

    /**
     * Get all liked song IDs for a user
     */
    suspend fun getLikedSongIds(userId: String): List<String> {
        return remote.getLikedSongIds(userId)
    }
}
