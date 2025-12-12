package com.example.doancuoikymobile.data.model

import androidx.room.Entity
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSong(
    val playlistId: String = "",
    val songId: String = "",
    val orderIndex: Int = 0
)

