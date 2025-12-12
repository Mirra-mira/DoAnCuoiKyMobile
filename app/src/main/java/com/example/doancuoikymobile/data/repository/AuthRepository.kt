package com.example.doancuoikymobile.data.repository

import android.util.Log
import com.example.doancuoikymobile.data.firebase.FirebaseAuthSource
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val authSource: FirebaseAuthSource
) {
    companion object {
        private const val TAG = "AUTH_REPO_DEBUG"
    }

    val currentUser get() = authSource.currentUser

    suspend fun signIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Bắt đầu đăng nhập cho email: $email")

        val result = authSource.signInWithEmail(email, password)

        Log.d(TAG, "Đăng nhập hoàn tất. Thành công: ${result.user != null}, User UID: ${result.user?.uid}")
        return@withContext result
    }

    suspend fun signUp(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Bắt đầu đăng ký cho email: $email")

        val result = authSource.signUpWithEmail(email, password)

        Log.d(TAG, "Đăng ký hoàn tất. Thành công: ${result.user != null}, User UID: ${result.user?.uid}")
        return@withContext result
    }

    suspend fun resetPassword(email: String): Unit = withContext(Dispatchers.IO) {
        Log.d(TAG, "Bắt đầu gửi yêu cầu đặt lại mật khẩu cho email: $email")
        authSource.sendPasswordReset(email)
        Log.d(TAG, "Yêu cầu đặt lại mật khẩu đã được gửi đến Firebase.")
    }

    fun signOut() {
        Log.d(TAG, "Bắt đầu đăng xuất.")
        authSource.signOut()
        Log.d(TAG, "Đã đăng xuất.")
    }
}