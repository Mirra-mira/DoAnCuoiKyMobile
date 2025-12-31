package com.example.doancuoikymobile.ui.home

import com.example.doancuoikymobile.model.Song

data class Genre(
    val id: String,
    val name: String,
    val imageUrl: String? = null
)

data class ContentCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String? = null,
    val type: ContentType,
    val song: Song? = null
)

enum class ContentType {
    SONG, ALBUM, PLAYLIST, ARTIST
}

sealed class HomeSection {
    data class Genres(val items: List<Genre>) : HomeSection()

    data class RecentlyPlayed(val items: List<RecentlyPlayedItem>) : HomeSection()

    data class Recommendations(
        val title: String,
        val items: List<RecommendationItem>
    ) : HomeSection()

    data class CustomSection(
        val title: String,
        val items: List<ContentCard>
    ) : HomeSection()
}

data class RecentlyPlayedItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String? = null,
    val type: ContentType,
    val playedAt: Long = System.currentTimeMillis(),
    val song: Song? = null
)

data class RecommendationItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String? = null,
    val type: ContentType
)