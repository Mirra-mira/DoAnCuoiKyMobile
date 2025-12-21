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
    val audioUrl: String = "", // Đây sẽ là link nhạc full từ API
    val coverUrl: String? = null,
    val mainArtistId: String? = null,
    val isOnline: Boolean = false // Thêm cờ này để biết nhạc từ API hay từ Firebase cá nhân
) : Serializable

fun SaavnSong.toSong(): Song {
    return Song(
        songId = this.id,
        title = this.name,
        duration = this.duration ?: 0,
        audioUrl = this.downloadUrl.lastOrNull()?.link ?: "",
        coverUrl = this.image.lastOrNull()?.link,
        mainArtistId = this.primaryArtists,
        isOnline = true
    )
}