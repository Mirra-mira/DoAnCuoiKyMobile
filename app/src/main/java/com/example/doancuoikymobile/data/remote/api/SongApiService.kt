package com.example.doancuoikymobile.data.remote.api

import com.example.doancuoikymobile.model.Song
import retrofit2.http.GET
import retrofit2.http.Query

interface SongApiService {
    @GET("search/songs")
    suspend fun searchSongs(@Query("query") query: String): SaavnResponse

    @GET("songs")
    suspend fun getSongDetail(@Query("id") id: String): SaavnResponse
}

data class SaavnResponse(
    val data: List<SaavnSong>? = null
)

data class SaavnSong(
    val id: String,
    val name: String,
    val duration: Int? = 0,
    val downloadUrl: List<DownloadLink>,
    val image: List<ImageLink>,
    val primaryArtists: String? = null
)

data class DownloadLink(val link: String, val quality: String? = null)
data class ImageLink(val link: String, val quality: String? = null)

fun SaavnSong.toSong(): Song {
    return Song(
        songId = this.id,
        title = this.name,
        duration = this.duration ?: 0,
        audioUrl = this.downloadUrl.lastOrNull()?.link ?: "",
        coverUrl = this.image.lastOrNull()?.link,
        mainArtistId = this.primaryArtists,
        isOnline = true
    )
}