package com.example.doancuoikymobile.data.model

import androidx.room.Entity
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "recently_played", primaryKeys = ["userId", "songId", "playedAt"])
data class RecentlyPlayed(
    val userId: String = "",
    val songId: String = "",
    val playedAt: Long = System.currentTimeMillis()
)

