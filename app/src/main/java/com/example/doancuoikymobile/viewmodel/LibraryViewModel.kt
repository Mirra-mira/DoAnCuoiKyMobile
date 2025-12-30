package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.model.Playlist
import com.example.doancuoikymobile.model.Artist
import com.example.doancuoikymobile.repository.PlaylistRepository
import com.example.doancuoikymobile.repository.ArtistRepository
import com.example.doancuoikymobile.data.remote.firebase.PlaylistRemoteDataSource
import com.example.doancuoikymobile.data.remote.firebase.PlaylistSongDataSource
import com.example.doancuoikymobile.data.remote.firebase.ArtistRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LibraryViewModel : ViewModel() {
    // Khởi tạo các Repository cần thiết
    private val firestore = FirebaseFirestore.getInstance()
    private val playlistRepo = PlaylistRepository(
        PlaylistRemoteDataSource(firestore),
        PlaylistSongDataSource(firestore)
    )

    private val artistRepo = ArtistRepository(ArtistRemoteDataSource(firestore))

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists

    // Hàm lấy dữ liệu từ Firestore dựa trên UserId [cite: 1203, 1116]
    fun loadLibraryData(userId: String) {
        viewModelScope.launch {
            // Lấy Playlist của User
            playlistRepo.watchUserPlaylists(userId).collect { list ->
                _playlists.value = list
            }
        }
        viewModelScope.launch {
            // Lấy toàn bộ Artist từ Firestore
            artistRepo.watchAll().collect { list ->
                _artists.value = list
            }
        }
    }
}