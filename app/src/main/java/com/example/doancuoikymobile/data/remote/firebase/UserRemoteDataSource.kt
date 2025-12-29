package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose

class UserRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersColl = firestore.collection("users")

    // Tạo Admin mặc định nếu chưa có (Dùng cho lần chạy đầu tiên)
    suspend fun createAdminIfNotExist() {
        val adminId = "admin_root"
        val doc = usersColl.document(adminId).get().await()
        if (!doc.exists()) {
            val adminUser = User(
                userId = adminId,
                username = "admin",
                email = "admin@app.com",
                displayName = "System Administrator",
                role = "admin"
            )
            usersColl.document(adminId).set(adminUser).await()
        }
    }

    suspend fun getUserOnce(userId: String): User? {
        val doc = usersColl.document(userId).get().await()
        return doc.toObject(User::class.java)?.copy(userId = doc.id)
    }

    fun watchUser(userId: String): Flow<User?> = callbackFlow {
        val registration = usersColl.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)?.copy(userId = snapshot.id)
                trySend(user).isSuccess
            }
        awaitClose { registration.remove() }
    }

    suspend fun upsertUser(user: User) {
        usersColl.document(user.userId).set(user).await()
    }

    suspend fun deleteUser(userId: String) {
        usersColl.document(userId).delete().await()
    }
}