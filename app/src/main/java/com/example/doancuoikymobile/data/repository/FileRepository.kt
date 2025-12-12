package com.example.doancuoikymobile.data.repository

import com.example.doancuoikymobile.data.firebase.StorageDataSource
import java.io.File

class FileRepository(
    private val storage: StorageDataSource
) {
    suspend fun uploadSongFile(local: File, remotePath: String): String = storage.uploadSongFile(local, remotePath)
    suspend fun uploadCover(local: File, remotePath: String): String = storage.uploadCover(local, remotePath)
    suspend fun delete(remotePath: String) = storage.delete(remotePath)
    suspend fun getDownloadUrl(remotePath: String): String = storage.getDownloadUrl(remotePath)
}
