package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.SongRemoteDataSource
import com.example.doancuoikymobile.data.remote.firebase.StorageDataSource
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.data.remote.api.SongApiService
import com.example.doancuoikymobile.data.remote.api.RetrofitClient
import com.example.doancuoikymobile.data.remote.api.toSong

class SongRepository {
    private val api = RetrofitClient.songApiService

    suspend fun searchSongs(query: String): List<Song> {
        return try {
            val response = api.searchSongs(query)
            response.data?.map { it.toSong() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSongById(id: String): Song? {
        return try {
            val response = api.getSongDetail(id)
            response.data?.firstOrNull()?.toSong()
        } catch (e: Exception) {
            null
        }
    }
}