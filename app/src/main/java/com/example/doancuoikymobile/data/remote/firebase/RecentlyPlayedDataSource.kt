package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.RecentlyPlayed
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose

class RecentlyPlayedDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun addRecord(record: RecentlyPlayed) {
        val userRecentColl = firestore.collection("users").document(record.userId)
            .collection("recentlyPlayed")
        userRecentColl.document(record.songId).set(record).await()
    }

    fun watchUserRecent(userId: String, limit: Long = 50): Flow<List<RecentlyPlayed>> = callbackFlow {
        val registration = firestore.collection("users").document(userId)
            .collection("recentlyPlayed")
            .orderBy("playedAt", Query.Direction.DESCENDING)  // Đảm bảo sắp xếp theo thời gian giảm dần (mới nhất trước)
            .limit(limit)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toObject(RecentlyPlayed::class.java) } ?: emptyList()
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }
}

