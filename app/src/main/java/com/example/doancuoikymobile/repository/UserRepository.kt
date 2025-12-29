package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.UserRemoteDataSource
import com.example.doancuoikymobile.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val remote: UserRemoteDataSource
) {
    // Gọi hàm này khi khởi động App (ví dụ trong SplashActivity hoặc Application class)
    suspend fun initializeAppSystem() {
        remote.createAdminIfNotExist()
    }

    /**
     * Xử lý đăng nhập Google.
     * Nếu User chưa tồn tại trong Firestore, coi như đăng ký mới.
     */
    suspend fun handleGoogleSignIn(firebaseUser: FirebaseUser): User {
        val existingUser = remote.getUserOnce(firebaseUser.uid)

        return if (existingUser != null) {
            existingUser
        } else {
            // Tạo mới nếu lần đầu đăng nhập bằng GG
            val newUser = User(
                userId = firebaseUser.uid,
                username = firebaseUser.email?.substringBefore("@") ?: "user",
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName,
                avatarUrl = firebaseUser.photoUrl?.toString(),
                role = "user" // Mặc định là user thường
            )
            remote.upsertUser(newUser)
            newUser
        }
    }

    suspend fun getUserOnce(userId: String): User? = remote.getUserOnce(userId)
    fun watchUser(userId: String): Flow<User?> = remote.watchUser(userId)
    suspend fun upsertUser(user: User) = remote.upsertUser(user)
    suspend fun deleteUser(userId: String) = remote.deleteUser(userId)
}