package com.example.doancuoikymobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
@Entity(tableName = "liked_songs")
data class LikedSong(
    @PrimaryKey
    val likeId: String = "",
    val userId: String = "",
    val songId: String = "",
    val likedAt: Long = System.currentTimeMillis()
) : Serializable
