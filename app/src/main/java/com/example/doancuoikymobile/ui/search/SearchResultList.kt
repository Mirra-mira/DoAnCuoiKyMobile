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
    onPlaylistClick: (playlistId: String, title: String) -> Unit,
    onSongClick: (Song) -> Unit,
    onArtistClick: (Artist) -> Unit = {}
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
                is SearchResultItem.Artist -> {
                    // Find the full artist object from the artists list
                    val artist = artists.find { it.artistId == result.id }
                    if (artist != null) {
                        ArtistItem(result, onClick = { onArtistClick(artist) })
                    } else {
                        ArtistItem(result)
                    }
                }
                is SearchResultItem.Playlist -> {
                    // Find the full playlist object from the playlists list
                    val playlist = playlists.find { it.playlistId == result.id }
                    if (playlist != null) {
                        PlaylistItem(
                            playlist = result,
                            onClick = { title: String, subtitle: String -> 
                                onPlaylistClick(playlist.playlistId, playlist.name) 
                            }
                        )
                    } else {
                        PlaylistItem(
                            playlist = result,
                            onClick = { title: String, subtitle: String -> 
                                onPlaylistClick(result.id, result.title) 
                            }
                        )
                    }
                }
            }
        }
    }
}