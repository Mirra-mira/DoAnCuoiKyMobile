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
    private val coll = firestore.collection("recently_played")

    suspend fun addRecord(record: RecentlyPlayed) {
        // Use auto-id or composite id
        coll.add(record).await()
    }

    fun watchUserRecent(userId: String, limit: Long = 50): Flow<List<RecentlyPlayed>> = callbackFlow {
        val registration = coll.whereEqualTo("userId", userId)
            .orderBy("playedAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toObject(RecentlyPlayed::class.java) } ?: emptyList()
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }
}
