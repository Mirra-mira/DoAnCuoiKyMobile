package com.example.doancuoikymobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey
    val playlistId: String = "",
    val userId: String = "",
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

