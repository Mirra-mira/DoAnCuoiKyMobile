package com.example.doancuoikymobile.ui.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.doancuoikymobile.model.Song

@Composable
fun HomeScreen(
    sections: List<HomeSection>,
    modifier: Modifier = Modifier,
    onSongClick: (Song) -> Unit = {},
    onPlaylistClick: (title: String, subtitle: String) -> Unit = { _, _ -> }
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        sections.forEach { section ->
            when (section) {
                is HomeSection.Genres -> {
                    item {
                        GenresSection(
                            genres = section.items,
                            onGenreClick = {  }
                        )
                    }
                }

                is HomeSection.RecentlyPlayed -> {
                    item {
                        RecentlyPlayedSection(
                            items = section.items,
                            onItemClick = { item ->
                                if (item.type == ContentType.SONG && item.song != null) {
                                    onSongClick(item.song)
                                } else {
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
                                if (item.type == ContentType.SONG && item.song != null) {
                                    onSongClick(item.song)
                                } else {
                                    onPlaylistClick(item.title, item.subtitle)
                                }
                            }
                        )
                    }
                }

                else -> {}
            }
        }
    }
}