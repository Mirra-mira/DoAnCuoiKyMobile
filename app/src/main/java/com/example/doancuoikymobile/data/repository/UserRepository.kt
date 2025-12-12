package com.example.doancuoikymobile.data.repository

import com.example.doancuoikymobile.data.firebase.UserRemoteDataSource
import com.example.doancuoikymobile.data.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val remote: UserRemoteDataSource
) {
    suspend fun getUserOnce(userId: String): User? = remote.getUserOnce(userId)
    fun watchUser(userId: String): Flow<User?> = remote.watchUser(userId)
    suspend fun upsertUser(user: User) = remote.upsertUser(user)
    suspend fun deleteUser(userId: String) = remote.deleteUser(userId)
}
