package com.example.doancuoikymobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
@Entity(tableName = "followed_artists")
data class FollowedArtist(
    @PrimaryKey
    val followId: String = "",
    val userId: String = "",
    val artistId: String = "",
    val followedAt: Long = System.currentTimeMillis()
) : Serializable
