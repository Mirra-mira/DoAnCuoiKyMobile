package com.example.doancuoikymobile.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class StorageDataSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val root = storage.reference

    suspend fun uploadSongFile(localFile: File, remotePath: String): String {
        val ref = root.child(remotePath)
        val uri = Uri.fromFile(localFile)
        val task = ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadCover(localFile: File, remotePath: String): String {
        val ref = root.child(remotePath)
        ref.putFile(Uri.fromFile(localFile)).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun getDownloadUrl(remotePath: String): String {
        return root.child(remotePath).downloadUrl.await().toString()
    }

    suspend fun delete(remotePath: String) {
        root.child(remotePath).delete().await()
    }

    suspend fun uploadAvatar(userId: String, imageUri: Uri): String? {
        return try {
            val ref = FirebaseStorage.getInstance().reference.child("avatars/$userId.jpg")
            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }
}
