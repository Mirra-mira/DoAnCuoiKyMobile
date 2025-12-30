package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.repository.PlaylistRepository
import com.example.doancuoikymobile.repository.SongRepository
import com.example.doancuoikymobile.data.remote.firebase.PlaylistRemoteDataSource
import com.example.doancuoikymobile.data.remote.firebase.PlaylistSongDataSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaylistDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

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
     */
    fun loadPlaylistSongs(playlistId: String) {
        viewModelScope.launch {
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