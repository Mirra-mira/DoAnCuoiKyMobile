package com.example.doancuoikymobile.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SongApiService {
    @GET("songs")
    suspend fun getSongs(@Query("limit") limit: Int = 50): Response<List<ApiSong>>

    @GET("songs/search")
    suspend fun searchSongs(@Query("q") query: String, @Query("limit") limit: Int = 50): Response<List<ApiSong>>
}

data class ApiSong(
    val id: String,
    val title: String,
    val duration: Int,
    val audioUrl: String,
    val coverUrl: String?,
    val mainArtist: String?
)

