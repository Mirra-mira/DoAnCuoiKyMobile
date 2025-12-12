package com.example.doancuoikymobile.data.repository

import com.example.doancuoikymobile.data.firebase.RecentlyPlayedDataSource
import com.example.doancuoikymobile.data.model.RecentlyPlayed
import kotlinx.coroutines.flow.Flow

class RecentlyPlayedRepository(
    private val remote: RecentlyPlayedDataSource
) {
    suspend fun addPlayed(record: RecentlyPlayed) = remote.addRecord(record)
    fun watchUserRecent(userId: String, limit: Long = 50): Flow<List<RecentlyPlayed>> = remote.watchUserRecent(userId, limit)
}
