package com.example.doancuoikymobile.model

data class SaavnResponse(
    val data: SaavnData
)

data class SaavnData(
    val results: List<SaavnSong>
)

data class SaavnSong(
    val id: String,
    val name: String,
    val duration: Int?,
    val image: List<UrlItem>,
    val downloadUrl: List<UrlItem>,
    val primaryArtists: String?
)

data class UrlItem(
    val link: String,
    val quality: String
)