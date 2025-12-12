package com.example.doancuoikymobile.data.model

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
    val duration: Int = 0, // seconds
    val audioUrl: String = "",
    val coverUrl: String? = null,
    val mainArtistId: String? = null,
    val previewUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

