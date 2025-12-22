package com.example.doancuoikymobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val role: String = "user",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

