package com.example.doancuoikymobile.data.repository

import com.example.doancuoikymobile.data.firebase.SongRemoteDataSource
import com.example.doancuoikymobile.data.firebase.StorageDataSource
import com.example.doancuoikymobile.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SongRepository(
    private val remote: SongRemoteDataSource,
    private val storage: StorageDataSource
) {
    fun watchAllSongs(): Flow<List<Song>> = remote.watchAllSongs()
    suspend fun getSong(songId: String): Song? = remote.getSongOnce(songId)
    suspend fun upsertSong(song: Song) = remote.upsertSong(song)
    suspend fun deleteSong(songId: String) = remote.deleteSong(songId)

    // upload local file and update song.audioUrl
    suspend fun uploadSongFileAndUpdateSong(localFile: java.io.File, remotePath: String, song: Song): Song {
        val url = storage.uploadSongFile(localFile, remotePath)
        val updated = song.copy(audioUrl = url)
        remote.upsertSong(updated)
        return updated
    }
}
