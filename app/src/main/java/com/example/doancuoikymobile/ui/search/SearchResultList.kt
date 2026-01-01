package com.example.doancuoikymobile.ui.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.model.Artist
import com.example.doancuoikymobile.model.Playlist
import com.example.doancuoikymobile.ui.components.ArtistItem
import com.example.doancuoikymobile.ui.components.PlaylistItem
import com.example.doancuoikymobile.ui.components.SongItem

@Composable
fun SearchResultsList(
    results: List<SearchResultItem>,
    songs: List<Song> = emptyList(),
    artists: List<Artist> = emptyList(),
    playlists: List<Playlist> = emptyList(),
    onPlaylistClick: (title: String, subtitle: String) -> Unit,
    onSongClick: (Song) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(results) { result ->
            when (result) {
                is SearchResultItem.Song -> {
                    // Find the full song object from the songs list
                    val song = songs.find { it.songId == result.id }
                    if (song != null) {
                        SongItem(result, onClick = { onSongClick(song) })
                    }
                }
                is SearchResultItem.Artist -> ArtistItem(result)
                is SearchResultItem.Playlist -> {
                    PlaylistItem(
                        playlist = result,
                        onClick = { title: String, subtitle: String -> onPlaylistClick(title, subtitle) }
                    )
                }
            }
        }
    }
}