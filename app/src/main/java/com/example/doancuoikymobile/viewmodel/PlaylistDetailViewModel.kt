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
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

class PlaylistDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val deezerApi: DeezerApiService = DeezerRetrofitClient.deezerApiService

    private val playlistRepo = PlaylistRepository(
        PlaylistRemoteDataSource(firestore),
        PlaylistSongDataSource(firestore)
    )
    private val songRepo = SongRepository()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    private val _playlistCoverUrl = MutableStateFlow<String?>(null)
    val playlistCoverUrl: StateFlow<String?> = _playlistCoverUrl.asStateFlow()

    private val _playlistTrackCount = MutableStateFlow<Int>(0)
    val playlistTrackCount: StateFlow<Int> = _playlistTrackCount.asStateFlow()

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
                    tracks.forEach { Log.d("DEBUG_PLAYLIST", "Song: ${it.title}, Link: ${it.previewUrl}") }
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

    // Truyền cả đối tượng Song vào thay vì chỉ ID
    fun addSongToPlaylist(playlistId: String, song: Song) {
        viewModelScope.launch {
            // 1. Đảm bảo thông tin chi tiết bài hát (bao gồm previewUrl) đã có trên Firebase
            songRepo.saveSongIfNotExists(song)

            // 2. Sau đó mới thêm liên kết vào playlist
            val exists = playlistRepo.isSongInPlaylist(playlistId, song.songId)
            if (!exists) {
                playlistRepo.addSongToPlaylist(
                    playlistId,
                    song.songId,
                    orderIndex = System.currentTimeMillis().toInt()
                )
            }
        }
    }

    suspend fun getUserPlaylists(): List<Playlist> {
        return playlistRepo.getUserPlaylists()
    }

    /**
     * Load playlist info (cover image, track count) from Deezer API
     */
    fun loadPlaylistInfo(playlistId: String) {
        viewModelScope.launch {
            val playlistIdLong = playlistId.toLongOrNull()
            
            if (playlistIdLong != null) {
                // This is a Deezer playlist, load info from Deezer API
                try {
                    val playlist = deezerApi.getPlaylist(playlistIdLong)
                    _playlistCoverUrl.value = playlist.picture_big ?: playlist.picture
                    _playlistTrackCount.value = playlist.nb_tracks
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                // Firebase playlist - no cover image from Deezer
                _playlistCoverUrl.value = null
            }
        }
    }
}