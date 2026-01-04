package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.FirebaseAuthSource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth

class AuthRepository {

    private val authSource = FirebaseAuthSource()
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = authSource.signIn(email, password)
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = authSource.signUp(email, password)
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val result = authSource.signInWithGoogle(idToken)
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return authSource.getCurrentUser()
    }

    fun signOut() {
        authSource.signOut()
    }

    suspend fun updatePassword(newPassword: String) {
        val user = firebaseAuth.currentUser
            ?: throw Exception("User not logged in")

        user.updatePassword(newPassword).await()
    }
}
