package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.PlaylistSong
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose

/**
 * Quản lý mối quan hệ many-to-many giữa playlists và bài hát.
 */
class PlaylistSongDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val coll = firestore.collection("playlist_songs")

    /**
     * Thêm bài hát vào playlist.
     */
    suspend fun addSongToPlaylist(playlistId: String, songId: String, orderIndex: Int = 0) {
        val docId = "${playlistId}_$songId"
        val ps = PlaylistSong(playlistId = playlistId, songId = songId, orderIndex = orderIndex)
        coll.document(docId).set(ps).await()
    }

    /**
     * Xóa bài hát khỏi playlist.
     */
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        val docId = "${playlistId}_$songId"
        coll.document(docId).delete().await()
    }

    /**
     * Lắng nghe danh sách bài hát trong playlist (Real-time).
     * LƯU Ý: Đã xóa .orderBy() để tránh lỗi yêu cầu Index từ Firebase.
     */
    fun watchPlaylistSongs(playlistId: String): Flow<List<PlaylistSong>> = callbackFlow {
        val registration = coll.whereEqualTo("playlistId", playlistId)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull {
                    it.toObject(PlaylistSong::class.java)
                } ?: emptyList()
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }

    /**
     * Cập nhật vị trí bài hát.
     */
    suspend fun updateSongOrderInPlaylist(playlistId: String, songId: String, newOrderIndex: Int) {
        val docId = "${playlistId}_$songId"
        coll.document(docId).update("orderIndex", newOrderIndex).await()
    }

    /**
     * Kiểm tra bài hát đã có trong playlist chưa.
     */
    suspend fun isSongInPlaylist(playlistId: String, songId: String): Boolean {
        val docId = "${playlistId}_$songId"
        val doc = coll.document(docId).get().await()
        return doc.exists()
    }
}