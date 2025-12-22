package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.UserRemoteDataSource
import com.example.doancuoikymobile.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val remote: UserRemoteDataSource
) {
    suspend fun getUserOnce(userId: String): User? = remote.getUserOnce(userId)
    fun watchUser(userId: String): Flow<User?> = remote.watchUser(userId)
    suspend fun upsertUser(user: User) = remote.upsertUser(user)
    suspend fun deleteUser(userId: String) = remote.deleteUser(userId)
}
