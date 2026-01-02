package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.data.remote.api.DeezerApiService
import com.example.doancuoikymobile.data.remote.api.DeezerRetrofitClient
import com.example.doancuoikymobile.data.remote.api.toSong
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.repository.PlaylistRepository
import com.example.doancuoikymobile.repository.SongRepository
import com.example.doancuoikymobile.data.remote.firebase.PlaylistRemoteDataSource
import com.example.doancuoikymobile.data.remote.firebase.PlaylistSongDataSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.doancuoikymobile.model.Playlist

class PlaylistDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val deezerApi: DeezerApiService = DeezerRetrofitClient.deezerApiService

    // Khởi tạo Repository với DataSource đã sửa
    private val playlistRepo = PlaylistRepository(
        PlaylistRemoteDataSource(firestore),
        PlaylistSongDataSource(firestore)
    )
    private val songRepo = SongRepository()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    /**
     * Tải danh sách bài hát và tự sắp xếp tại Local.
     * Hỗ trợ cả Firebase playlist và Deezer playlist.
     */
    fun loadPlaylistSongs(playlistId: String) {
        viewModelScope.launch {
            // Check if this is a Deezer playlist (userId is empty for Deezer playlists)
            // Try to parse as Long to see if it's a Deezer ID
            val playlistIdLong = playlistId.toLongOrNull()
            
            if (playlistIdLong != null) {
                // This is likely a Deezer playlist, load from Deezer API
                try {
                    val response = deezerApi.getPlaylistTracks(playlistIdLong, limit = 100)
                    val tracks = response.data.map { it.toSong() }
                    _songs.value = tracks
                } catch (e: Exception) {
                    e.printStackTrace()
                    _songs.value = emptyList()
                }
            } else {
                // This is a Firebase playlist, load from Firebase
                playlistRepo.watchPlaylistSongs(playlistId).collect { playlistSongs ->
                    val sortedList = playlistSongs.sortedBy { it.orderIndex }

                    val detailedSongs = mutableListOf<Song>()
                    for (ps in sortedList) {
                        val song = songRepo.getSongById(ps.songId)
                        if (song != null) {
                            detailedSongs.add(song)
                        }
                    }

                    // Cập nhật lên UI
                    _songs.value = detailedSongs
                }
            }
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            val exists = playlistRepo.isSongInPlaylist(playlistId, songId)
            if (!exists) {
                playlistRepo.addSongToPlaylist(
                    playlistId,
                    songId,
                    orderIndex = System.currentTimeMillis().toInt()
                )
            }
        }
    }

    suspend fun getUserPlaylists(): List<Playlist> {
        return playlistRepo.getUserPlaylists()
    }
}