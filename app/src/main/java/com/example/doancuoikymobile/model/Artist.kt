package com.example.doancuoikymobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
@Entity(tableName = "artists")
data class Artist(
    @PrimaryKey
    val artistId: String = "",
    val name: String = "",
    val pictureUrl: String? = null,
    val searchKeywords: List<String> = emptyList() // For Firestore search
) : Serializable
