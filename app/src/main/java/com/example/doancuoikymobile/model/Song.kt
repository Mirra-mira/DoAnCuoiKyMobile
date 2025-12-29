package com.example.doancuoikymobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey
    val songId: String = "",
    val title: String = "",
    val duration: Int = 0, 
    val audioUrl: String = "", // Full MP3 URL (user uploaded hoặc API full track)
    val previewUrl: String? = null, // Deezer 30s preview hoặc sample audio
    val coverUrl: String? = null,
    val mainArtistId: String? = null,
    val isOnline: Boolean = false // Thêm cờ này để biết nhạc từ API hay từ Firebase cá nhân
) : Serializable