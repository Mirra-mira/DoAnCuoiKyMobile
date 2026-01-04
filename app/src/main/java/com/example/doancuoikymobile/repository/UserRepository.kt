package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.UserRemoteDataSource
import com.example.doancuoikymobile.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose

class UserRepository(
    private val remote: UserRemoteDataSource
) {
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

    suspend fun uploadAvatar(
        userId: String,
        imageUri: android.net.Uri
    ): String? {
        return remote.uploadAvatar(userId, imageUri)
    }

    suspend fun getUserStats(userId: String): Triple<Int, Int, Int> {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        return try {
            // Đếm Playlists của user
            val playlists = com.google.android.gms.tasks.Tasks.await(
                db.collection("playlists").whereEqualTo("ownerId", userId).get()
            ).size()

            // Đếm người đang theo dõi user này
            val followers = com.google.android.gms.tasks.Tasks.await(
                db.collection("follows").whereEqualTo("followingId", userId).get()
            ).size()

            // Đếm người mà user này đang theo dõi
            val following = com.google.android.gms.tasks.Tasks.await(
                db.collection("follows").whereEqualTo("followerId", userId).get()
            ).size()

            Triple(playlists, followers, following)
        } catch (e: Exception) {
            Triple(0, 0, 0)
        }
    }

    // Thêm vào class UserRepository
    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    // 1. Lắng nghe thay đổi số lượng Real-time bằng Flow
    fun watchUserStats(userId: String): Flow<Triple<Int, Int, Int>> = kotlinx.coroutines.flow.callbackFlow {
        val playlistQuery = db.collection("playlists").whereEqualTo("ownerId", userId)
        val followersQuery = db.collection("follows").whereEqualTo("followingId", userId)
        val followingQuery = db.collection("follows").whereEqualTo("followerId", userId)

        val stats = mutableMapOf("p" to 0, "fer" to 0, "ing" to 0)

        val sendStats = {
            trySend(Triple(stats["p"] ?: 0, stats["fer"] ?: 0, stats["ing"] ?: 0))
        }

        val reg1 = playlistQuery.addSnapshotListener { s, _ -> stats["p"] = s?.size() ?: 0; sendStats() }
        val reg2 = followersQuery.addSnapshotListener { s, _ -> stats["fer"] = s?.size() ?: 0; sendStats() }
        val reg3 = followingQuery.addSnapshotListener { s, _ -> stats["ing"] = s?.size() ?: 0; sendStats() }

        awaitClose {
            reg1.remove(); reg2.remove(); reg3.remove()
        }
    }

    // 2. Logic nhấn nút Follow/Unfollow
    suspend fun toggleFollow(currentId: String, targetId: String) {
        val docRef = db.collection("follows").document("${currentId}_${targetId}")
        val doc = com.google.android.gms.tasks.Tasks.await(docRef.get())

        if (doc.exists()) {
            docRef.delete()
        } else {
            val data = mapOf("followerId" to currentId, "followingId" to targetId)
            docRef.set(data)
        }
    }
}