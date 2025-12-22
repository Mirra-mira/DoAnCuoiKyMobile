package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.StorageDataSource
import java.io.File

/**
 * DEPRECATED: Use SongRepository or ArtistRepository instead.
 * 
 * This repository duplicates functionality from other repositories.
 * For file uploads, use:
 * - SongRepository.uploadSongFileAndUpdateSong() for MP3 files
 * - StorageDataSource directly for cover images (or extend SongRepository)
 */
@Deprecated("Use SongRepository for song uploads", ReplaceWith("SongRepository"))
class FileRepository(
    private val storage: StorageDataSource
) {
    suspend fun uploadSongFile(local: File, remotePath: String): String = storage.uploadSongFile(local, remotePath)
    suspend fun uploadCover(local: File, remotePath: String): String = storage.uploadCover(local, remotePath)
    suspend fun delete(remotePath: String) = storage.delete(remotePath)
    suspend fun getDownloadUrl(remotePath: String): String = storage.getDownloadUrl(remotePath)
}
