package com.example.doancuoikymobile.ui.home

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
    val type: ContentType
)

enum class ContentType {
    SONG, ALBUM, PLAYLIST, ARTIST
}

// SEALED CLASS: Đây chính là thứ bạn đang thiếu
sealed class HomeSection {
    // Đại diện cho hàng "Thể loại"
    data class Genres(val items: List<Genre>) : HomeSection()

    // Đại diện cho hàng "Nghe gần đây"
    data class RecentlyPlayed(val items: List<RecentlyPlayedItem>) : HomeSection()

    // Đại diện cho hàng "Dành cho bạn"
    data class Recommendations(
        val title: String,
        val items: List<RecommendationItem>
    ) : HomeSection()

    // Đại diện cho các hàng tùy chỉnh (Phát hành mới, Playlist phổ biến)
    data class CustomSection(
        val title: String,
        val items: List<ContentCard>
    ) : HomeSection()
}

// Các class hỗ trợ khác
data class RecentlyPlayedItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String? = null,
    val type: ContentType,
    val playedAt: Long = System.currentTimeMillis()
)

data class RecommendationItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String? = null,
    val type: ContentType
)