package com.example.doancuoikymobile.data.model

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
    val pictureUrl: String? = null
) : Serializable
