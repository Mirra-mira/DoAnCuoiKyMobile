package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.model.Playlist
import com.example.doancuoikymobile.model.Artist
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.repository.PlaylistRepository
import com.example.doancuoikymobile.repository.ArtistRepository
import com.example.doancuoikymobile.repository.SongRepository
import com.example.doancuoikymobile.data.remote.firebase.PlaylistRemoteDataSource
import com.example.doancuoikymobile.data.remote.firebase.PlaylistSongDataSource
import com.example.doancuoikymobile.data.remote.firebase.ArtistRemoteDataSource
import com.example.doancuoikymobile.repository.RecentlyPlayedRepository
import com.example.doancuoikymobile.data.remote.firebase.RecentlyPlayedDataSource
import com.example.doancuoikymobile.model.RecentlyPlayed
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class LibraryViewModel : ViewModel() {
    // Khởi tạo các Repository cần thiết
    private val firestore = FirebaseFirestore.getInstance()
    private val playlistRepo = PlaylistRepository(
        PlaylistRemoteDataSource(firestore),
        PlaylistSongDataSource(firestore)
    )

    private val recentlyPlayedRepo = RecentlyPlayedRepository(
        RecentlyPlayedDataSource(firestore)
    )

    private val artistRepo = ArtistRepository(ArtistRemoteDataSource(firestore))

    private val songRepo = SongRepository()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs

    private val _recentlyPlayed = MutableStateFlow<List<RecentlyPlayed>>(emptyList())
    val recentlyPlayed: StateFlow<List<RecentlyPlayed>> = _recentlyPlayed

    private var libraryDataLoadedForUser: String? = null  // Cache để tránh load lặp

    // Hàm lấy dữ liệu từ Firestore dựa trên UserId [cite: 1203, 1116]
    fun loadLibraryData(userId: String) {
        // Chỉ load một lần cho mỗi user
        if (libraryDataLoadedForUser == userId) {
            return
        }
        libraryDataLoadedForUser = userId

        viewModelScope.launch {
            // Theo dõi Playlist của User
            playlistRepo.watchUserPlaylists(userId).collect { list ->
                _playlists.value = list
            }
        }

        viewModelScope.launch {
            // Theo dõi tất cả Artists (lỗi: nên theo dõi Artists mà User đã follow, không lấy tất cả)
            // TODO: Thay đổi thành watchFollowedArtists khi có feature follow artists
            artistRepo.watchAll().collect { list ->
                _artists.value = list
            }
        }

        viewModelScope.launch {
            // Theo dõi tất cả Songs
            songRepo.getAllSongs().collect { list ->
                _allSongs.value = list
            }
        }

        viewModelScope.launch {
            recentlyPlayedRepo
            .watchUserRecent(userId)
            .collect { list ->
            _recentlyPlayed.value = list
            }
        }
    }

    // Tạo Playlist mới và broadcast qua Flow
    fun createPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepo.upsertPlaylist(playlist)
            // watchUserPlaylists tự động phát sự kiện khi dữ liệu thay đổi
        }
    }
}