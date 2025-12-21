package com.example.doancuoikymobile.model

import androidx.room.Entity
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(
    tableName = "song_artists",
    primaryKeys = ["songId", "artistId"]
)
data class SongArtist(
    val songId: String = "",
    val artistId: String = "",
    val role: String = "main" // main / feat / producer
)
