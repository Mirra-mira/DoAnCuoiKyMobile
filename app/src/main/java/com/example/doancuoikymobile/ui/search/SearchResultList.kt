package com.example.doancuoikymobile.ui.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchResultsList(results: List<SearchResultItem>) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(results) { result ->
            when (result) {
                is SearchResultItem.Song -> SongItem(result)
                is SearchResultItem.Artist -> ArtistItem(result)
                is SearchResultItem.Playlist -> PlaylistItem(result)
            }
        }
    }
}