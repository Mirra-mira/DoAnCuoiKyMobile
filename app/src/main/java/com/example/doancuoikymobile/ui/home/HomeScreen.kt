package com.example.doancuoikymobile.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember

data class Genre(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    //val color: String = MaterialTheme.colorScheme.background
)

/**
 * Recently Played Item
 */
data class RecentlyPlayedItem(
    val id: String,
    val title: String,
    val subtitle: String, // Artist - "Playlist"
    val imageUrl: String? = null,
    val type: ContentType,
    val playedAt: Long = System.currentTimeMillis()
)

/**
 * Recommendation Item (Picks for You)
 */
data class RecommendationItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String? = null,
    val type: ContentType
)

/**
 * Album/Playlist Card
 */
data class ContentCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String? = null,
    val type: ContentType
)

/**
 * Type của content
 */
enum class ContentType {
    SONG,
    ALBUM,
    PLAYLIST,
    ARTIST
}

/**
 *  Home screen
 */
sealed class HomeSection {
    data class Genres(val items: List<Genre>) : HomeSection()
    data class RecentlyPlayed(val items: List<RecentlyPlayedItem>) : HomeSection()
    data class Recommendations(
        val title: String, // "Picks for {username}"
        val items: List<RecommendationItem>
    ) : HomeSection()
    data class CustomSection(
        val title: String, // "Popular playlists", "New releases", etc.
        val items: List<ContentCard>
    ) : HomeSection()
}



@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    username: String = "Bạn",
    isNewUser: Boolean = false,
    onSongClick: (String) -> Unit = {},
    onPlaylistClick: (title: String, subtitle: String) -> Unit = { _, _ -> }
) {
    // Mock sections

    val sections = remember(username, isNewUser) {
        buildHomeSections(username, isNewUser)
    }


    LazyColumn(
    ) {
        sections.forEach { section ->
            when (section) {
                is HomeSection.Genres -> {
                    item {
                        GenresSection(
                            genres = section.items,
                            onGenreClick = { genre ->
                                // TODO: Navigate to genre detail
                            }
                        )
                    }
                }

                is HomeSection.RecentlyPlayed -> {
                    item {
                        RecentlyPlayedSection(
                            items = section.items,
                            onItemClick = { item ->
                                if (item.type == ContentType.SONG) {
                                    onSongClick(item.title)
                                } else if (item.type == ContentType.PLAYLIST) {
                                    onPlaylistClick(item.title, item.subtitle)
                                } else {
                                    //
                                }
                            }
                        )
                    }
                }

                is HomeSection.Recommendations -> {
                    item {
                        RecommendationsSection(
                            title = section.title,
                            items = section.items,
                            onItemClick = { item ->
                                if (item.type == ContentType.SONG) {
                                    onSongClick(item.title)
                                } else if (item.type == ContentType.PLAYLIST) {
                                    onPlaylistClick(item.title, item.subtitle)
                                }
                            }
                        )
                    }
                }

                is HomeSection.CustomSection -> {
                    item {
                        CustomSection(
                            title = section.title,
                            items = section.items,
                            onItemClick = { item ->
                                if (item.type == ContentType.SONG) {
                                    onSongClick(item.title)
                                } else if (item.type == ContentType.PLAYLIST) {
                                    onPlaylistClick(item.title, item.subtitle)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Build sections dựa trên user state
 */
private fun buildHomeSections(
    username: String,
    isNewUser: Boolean
): List<HomeSection> {
    val sections = mutableListOf<HomeSection>()


    sections.add(
        HomeSection.Genres(
            items = listOf(
                Genre("1", "V-Pop"),
                Genre("2", "Nhạc Trẻ"),
                Genre("3", "Ballad"),
                Genre("4", "Rap Việt"),
                Genre("5", "EDM"),
                Genre("6", "Acoustic")
            )
        )
    )

    // 2. Recently Played - CHỈ hiện nếu KHÔNG phải user mới
    if (!isNewUser) {
        sections.add(
            HomeSection.RecentlyPlayed(
                items = listOf(
                    RecentlyPlayedItem(
                        "r1", "Lạc Trôi", "Sơn Tùng M-TP",
                        type = ContentType.SONG
                    ),
                    RecentlyPlayedItem(
                        "r2", "Chill V-Pop", "Playlist • 50 bài",
                        type = ContentType.PLAYLIST
                    ),
                    RecentlyPlayedItem(
                        "r3", "Bài Này Chill Phết", "Đen Vâu",
                        type = ContentType.SONG
                    ),
                    RecentlyPlayedItem(
                        "r4", "V-Pop Hits", "Playlist • 100 bài",
                        type = ContentType.PLAYLIST
                    ),
                    RecentlyPlayedItem(
                        "r5", "Hoàng Thùy Linh", "Nghệ sĩ",
                        type = ContentType.ARTIST
                    )
                )
            )
        )
    }

    // 3. Picks for You - LUÔN hiển thị
    sections.add(
        HomeSection.Recommendations(
            title = if (isNewUser) "Gợi ý cho bạn" else "Dành cho $username",
            items = listOf(
                RecommendationItem(
                    "p1", "Daily Mix 1",
                    "Sơn Tùng M-TP, Đen Vâu và nhiều hơn nữa",
                    type = ContentType.PLAYLIST
                ),
                RecommendationItem(
                    "p2", "Discover Weekly",
                    "Khám phá âm nhạc mới mỗi tuần",
                    type = ContentType.PLAYLIST
                ),
                RecommendationItem(
                    "p3", "Release Radar",
                    "Những bản phát hành mới từ nghệ sĩ bạn theo dõi",
                    type = ContentType.PLAYLIST
                ),
                RecommendationItem(
                    "p4", "Daily Mix 2",
                    "Hoàng Thùy Linh, Bích Phương và nhiều hơn nữa",
                    type = ContentType.PLAYLIST
                )
            )
        )
    )

    // 4. Popular Playlists - LUÔN hiển thị
    sections.add(
        HomeSection.CustomSection(
            title = "Playlist phổ biến",
            items = listOf(
                ContentCard(
                    "c1", "Top 50 Vietnam",
                    "Những bài hát hot nhất tại Việt Nam",
                    type = ContentType.PLAYLIST
                ),
                ContentCard(
                    "c2", "V-Pop Rising",
                    "Nghệ sĩ V-Pop đang lên",
                    type = ContentType.PLAYLIST
                ),
                ContentCard(
                    "c3", "Chill Hits",
                    "Thư giãn với những bản hit chill",
                    type = ContentType.PLAYLIST
                ),
                ContentCard(
                    "c4", "Workout Beats",
                    "Năng lượng cho buổi tập",
                    type = ContentType.PLAYLIST
                )
            )
        )
    )

    // 5. New Releases - LUÔN hiển thị
    sections.add(
        HomeSection.CustomSection(
            title = "Phát hành mới",
            items = listOf(
                ContentCard(
                    "n1", "Making My Way",
                    "Sơn Tùng M-TP",
                    type = ContentType.ALBUM
                ),
                ContentCard(
                    "n2", "See Tình",
                    "Hoàng Thùy Linh",
                    type = ContentType.ALBUM
                ),
                ContentCard(
                    "n3", "Trống Rỗng",
                    "Đen Vâu",
                    type = ContentType.SONG
                ),
                ContentCard(
                    "n4", "Đi Giữa Trời Rực Rỡ",
                    "Ngô Lan Hương",
                    type = ContentType.SONG
                )
            )
        )
    )

    return sections
}