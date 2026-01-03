package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.data.remote.api.DeezerArtistDataSource
import com.example.doancuoikymobile.data.remote.api.DeezerApiService
import com.example.doancuoikymobile.data.remote.api.DeezerRetrofitClient
import com.example.doancuoikymobile.data.remote.api.toSong
import com.example.doancuoikymobile.model.Artist
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.repository.ArtistRepository
import com.example.doancuoikymobile.repository.FollowedArtistRepository
import com.example.doancuoikymobile.data.remote.firebase.ArtistRemoteDataSource
import com.example.doancuoikymobile.data.remote.firebase.FollowedArtistRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArtistDetailViewModel : ViewModel() {
    private val deezerApi: DeezerApiService = DeezerRetrofitClient.deezerApiService
    private val deezerArtistDataSource: DeezerArtistDataSource = DeezerArtistDataSource()
    private val firestore = FirebaseFirestore.getInstance()
    private val artistRepo = ArtistRepository(ArtistRemoteDataSource(firestore))
    private val followedArtistRepo = FollowedArtistRepository(FollowedArtistRemoteDataSource(firestore))

    private val _artist = MutableStateFlow<Artist?>(null)
    val artist: StateFlow<Artist?> = _artist.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isFollowed = MutableStateFlow(false)
    val isFollowed: StateFlow<Boolean> = _isFollowed.asStateFlow()

    fun loadArtist(artistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val artistIdLong = artistId.toLongOrNull()
                if (artistIdLong != null) {
                    // Load artist details
                    val artist = deezerArtistDataSource.getArtist(artistIdLong)
                    _artist.value = artist

                    // Load artist top tracks
                    val response = deezerApi.getArtistTopTracks(artistIdLong, limit = 50)
                    val tracks = response.data.map { it.toSong() }
                    _songs.value = tracks
                } else {
                    _error.value = "Invalid artist ID"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading artist"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkFollowStatus(userId: String, artistId: String) {
        viewModelScope.launch {
            try {
                val isFollowed = followedArtistRepo.isFollowed(userId, artistId)
                _isFollowed.value = isFollowed
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleFollow(userId: String, artist: Artist) {
        viewModelScope.launch {
            try {
                artistRepo.upsertArtist(artist)
                val isCurrentlyFollowed = _isFollowed.value
                followedArtistRepo.toggleFollowArtist(userId, artist.artistId, isCurrentlyFollowed)
                _isFollowed.value = !isCurrentlyFollowed
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

