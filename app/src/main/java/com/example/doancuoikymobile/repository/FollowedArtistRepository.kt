package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.FollowedArtistRemoteDataSource
import com.example.doancuoikymobile.model.FollowedArtist
import kotlinx.coroutines.flow.Flow

class FollowedArtistRepository(private val remote: FollowedArtistRemoteDataSource) {

    /**
     * Watch user's followed artists in real-time
     */
    fun watchUserFollowedArtists(userId: String): Flow<List<FollowedArtist>> {
        return remote.watchUserFollowedArtists(userId)
    }

    /**
     * Toggle follow status for an artist
     */
    suspend fun toggleFollowArtist(userId: String, artistId: String, isCurrentlyFollowed: Boolean): Boolean {
        return if (isCurrentlyFollowed) {
            remote.unfollowArtist(userId, artistId)
        } else {
            remote.followArtist(userId, artistId)
        }
    }

    /**
     * Follow an artist
     */
    suspend fun followArtist(userId: String, artistId: String): Boolean {
        return remote.followArtist(userId, artistId)
    }

    /**
     * Unfollow an artist
     */
    suspend fun unfollowArtist(userId: String, artistId: String): Boolean {
        return remote.unfollowArtist(userId, artistId)
    }

    /**
     * Check if user follows an artist
     */
    suspend fun isFollowed(userId: String, artistId: String): Boolean {
        return remote.isFollowedByUser(userId, artistId)
    }

    /**
     * Get all followed artist IDs for a user
     */
    suspend fun getFollowedArtistIds(userId: String): List<String> {
        return remote.getFollowedArtistIds(userId)
    }
}
