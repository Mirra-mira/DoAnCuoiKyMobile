package com.example.doancuoikymobile.model

import androidx.room.Entity
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "favorite_songs", primaryKeys = ["userId", "songId"])
data class FavoriteSong(
    val userId: String = "",
    val songId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

