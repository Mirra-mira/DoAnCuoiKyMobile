package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.FirebaseAuthSource
import com.google.firebase.auth.FirebaseUser

class AuthRepository {

    private val authSource = FirebaseAuthSource()

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
}
