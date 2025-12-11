package com.example.doancuoikymobile.ui.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.doancuoikymobile.ui.components.ArtistItem
import com.example.doancuoikymobile.ui.components.PlaylistItem
import com.example.doancuoikymobile.ui.components.SongItem

@Composable
fun SearchResultsList(results: List<SearchResultItem>,
                       onPlaylistClick: (title: String, subtitle: String) -> Unit,
                       onSongClick: (String) -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(results) { result ->
            when (result) {
                is SearchResultItem.Song -> SongItem(
                    result,
                    onClick = { onSongClick(result.title) })
                is SearchResultItem.Artist -> ArtistItem(result)
                is SearchResultItem.Playlist -> PlaylistItem(
                    result,
                    onClick = { title, subtitle -> onPlaylistClick(result.title, result.subtitle) })
            }
        }
    }
}