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
import com.example.doancuoikymobile.repository.LikedSongRepository
import com.example.doancuoikymobile.data.remote.firebase.LikedSongRemoteDataSource
import com.example.doancuoikymobile.repository.FollowedArtistRepository
import com.example.doancuoikymobile.data.remote.firebase.FollowedArtistRemoteDataSource
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

    private val likedSongRepo = LikedSongRepository(LikedSongRemoteDataSource(firestore))

    private val followedArtistRepo = FollowedArtistRepository(FollowedArtistRemoteDataSource(firestore))

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs

    private val _recentlyPlayed = MutableStateFlow<List<RecentlyPlayed>>(emptyList())
    val recentlyPlayed: StateFlow<List<RecentlyPlayed>> = _recentlyPlayed

    private val _likedSongs = MutableStateFlow<List<Song>>(emptyList())
    val likedSongs: StateFlow<List<Song>> = _likedSongs

    private val _likedSongIds = MutableStateFlow<List<String>>(emptyList())
    val likedSongIds: StateFlow<List<String>> = _likedSongIds

    private val _followedArtistIds = MutableStateFlow<List<String>>(emptyList())
    val followedArtistIds: StateFlow<List<String>> = _followedArtistIds

    private val _followedArtists = MutableStateFlow<List<Artist>>(emptyList())
    val followedArtists: StateFlow<List<Artist>> = _followedArtists

    private var libraryDataLoadedForUser: String? = null  // Cache để tránh load lặp
    private var currentUserId: String? = null

    // Hàm lấy dữ liệu từ Firestore dựa trên UserId [cite: 1203, 1116]
    fun loadLibraryData(userId: String) {
        if (libraryDataLoadedForUser == userId) return
        libraryDataLoadedForUser = userId
        currentUserId = userId

        viewModelScope.launch {
            // Theo dõi Playlist của User
            playlistRepo.watchUserPlaylists(userId).collect { list ->
                _playlists.value = list
            }
        }

        viewModelScope.launch {
            // Theo dõi tất cả Artists
            artistRepo.watchAll().collect { list ->
                _artists.value = list
            }
        }

        viewModelScope.launch {
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

        // Watch liked songs with real-time updates
        viewModelScope.launch {
            combine(likedSongRepo.watchUserLikedSongs(userId), _allSongs) { likedObjects, allSongsList ->
                val likedIds = likedObjects.map { it.songId }
                _likedSongIds.value = likedIds
                // Lọc trực tiếp từ allSongsList mới nhất vừa nhận được
                allSongsList.filter { it.songId in likedIds }
            }.collect { filteredList ->
                _likedSongs.value = filteredList
            }
        }

        // Watch followed artists with real-time updates
        viewModelScope.launch {
            combine(followedArtistRepo.watchUserFollowedArtists(userId), _artists) { followedObjects, allArtists ->
                val followedIds = followedObjects.map { it.artistId }
                _followedArtistIds.value = followedIds
                allArtists.filter { it.artistId in followedIds }
            }.collect { filteredArtists ->
                _followedArtists.value = filteredArtists
            }
        }
    }

    // Hàm để thêm/xóa bài hát yêu thích
    fun toggleLikeSong(userId: String, songId: String) {
        viewModelScope.launch {
            val isCurrentlyLiked = _likedSongIds.value.contains(songId)
            likedSongRepo.toggleLikeSong(userId, songId, isCurrentlyLiked)
        }
    }

    // Hàm để theo dõi/bỏ theo dõi nghệ sĩ
    fun toggleFollowArtist(userId: String, artistId: String) {
        viewModelScope.launch {
            val isCurrentlyFollowed = _followedArtistIds.value.contains(artistId)
            followedArtistRepo.toggleFollowArtist(userId, artistId, isCurrentlyFollowed)
        }
    }

    // Tạo Playlist mới và broadcast qua Flow
    fun createPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepo.upsertPlaylist(playlist)
            // watchUserPlaylists tự động phát sự kiện khi dữ liệu thay đổi
        }
    }

    // Lấy danh sách playlist hiện tại (dạng list thường để dùng cho Dialog)
    fun getCurrentPlaylists(): List<Playlist> {
        return _playlists.value
    }

    // Thêm bài hát vào playlist đã chọn
    fun addSongToPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            playlistRepo.addSongToPlaylist(playlistId, songId)
        }
    }
}