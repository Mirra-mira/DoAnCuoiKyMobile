package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.model.RecentlyPlayed
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.player.PlayerManager
import com.example.doancuoikymobile.repository.ArtistRepository
import com.example.doancuoikymobile.repository.AuthRepository
import com.example.doancuoikymobile.repository.RecentlyPlayedRepository
import com.example.doancuoikymobile.repository.SongRepository
import com.example.doancuoikymobile.data.remote.firebase.ArtistRemoteDataSource
import com.example.doancuoikymobile.data.remote.firebase.RecentlyPlayedDataSource
import com.example.doancuoikymobile.repository.LikedSongRepository
import com.example.doancuoikymobile.data.remote.firebase.LikedSongRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val artistRepository: ArtistRepository = ArtistRepository(ArtistRemoteDataSource()),
    private val songRepository: SongRepository = SongRepository(),
    private val recentlyPlayedRepository: RecentlyPlayedRepository = RecentlyPlayedRepository(RecentlyPlayedDataSource()),
    private val authRepository: AuthRepository = AuthRepository(),
    private val likedSongRepository: LikedSongRepository = LikedSongRepository(LikedSongRemoteDataSource(FirebaseFirestore.getInstance()))
) : ViewModel() {

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    // State theo dõi trạng thái Like của bài hát hiện tại
    private val _isCurrentSongLiked = MutableStateFlow(false)
    val isCurrentSongLiked = _isCurrentSongLiked.asStateFlow()

    private val _artistName = MutableStateFlow<String>("")
    val artistName = _artistName.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(0)
    val repeatMode = _repeatMode.asStateFlow()

    private val playlist = mutableListOf<Song>()
    private var currentPlaylistIndex = -1

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            while (true) {
                val song = PlayerManager.getCurrentSong()

                if (song?.songId != _currentSong.value?.songId) {
                    _currentSong.value = song
                    _artistName.value = song?.artistName ?: ""
                    song?.let { checkLikeStatus(it.songId) }
                }

                _isPlaying.value = PlayerManager.isPlaying()
                _progress.value = PlayerManager.getCurrentPosition()
                _duration.value = PlayerManager.getDuration()

                if (_duration.value > 0 &&
                    _progress.value >= _duration.value - 500 &&
                    _isPlaying.value
                ) {
                    when (_repeatMode.value) {
                        1 -> playNext()
                        2 -> _currentSong.value?.let { playSong(it) }
                    }
                }
                delay(1000)
            }
        }
    }

    private fun checkLikeStatus(songId: String) {
        val userId = authRepository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isCurrentSongLiked.value = likedSongRepository.isLiked(userId, songId)
        }
    }

    fun toggleLikeCurrentSong() {
        val song = _currentSong.value ?: return
        val userId = authRepository.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            val isCurrentlyLiked = _isCurrentSongLiked.value
            val success = likedSongRepository.toggleLikeSong(userId, song.songId, isCurrentlyLiked)
            if (success) {
                _isCurrentSongLiked.value = !isCurrentlyLiked
            }
        }
    }

    fun togglePlayPause() {
        if (PlayerManager.isPlaying()) {
            PlayerManager.pause()
        } else {
            PlayerManager.play()
        }
    }

    fun seekTo(position: Long) {
        PlayerManager.seekTo(position)
    }

    fun playNext() {
        if (playlist.isEmpty()) return

        currentPlaylistIndex = if (_isShuffle.value && playlist.size > 1) {
            (playlist.indices - currentPlaylistIndex).random()
        } else {
            (currentPlaylistIndex + 1) % playlist.size
        }

        playSong(playlist[currentPlaylistIndex])
    }

    fun playPrevious() {
        if (playlist.isEmpty()) return

        currentPlaylistIndex = if (_isShuffle.value && playlist.size > 1) {
            (playlist.indices - currentPlaylistIndex).random()
        } else {
            if (currentPlaylistIndex - 1 < 0) playlist.size - 1
            else currentPlaylistIndex - 1
        }

        playSong(playlist[currentPlaylistIndex])
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = (_repeatMode.value + 1) % 3
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        playlist.clear()
        playlist.addAll(songs)
        currentPlaylistIndex = startIndex
        if (startIndex in playlist.indices) {
            playSong(playlist[startIndex])
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            var playedSong = song
            if (song.audioUrl.isEmpty() && song.songId.isNotEmpty()) {
                val firebaseSong = songRepository.getSongById(song.songId)
                if (firebaseSong != null && firebaseSong.audioUrl.isNotEmpty()) {
                    playedSong = firebaseSong
                }
            }

            val urlToPlay = when {
                playedSong.audioUrl.isNotEmpty() -> playedSong.audioUrl
                !playedSong.previewUrl.isNullOrEmpty() -> playedSong.previewUrl
                else -> null
            }

            if (urlToPlay != null) {
                PlayerManager.playSong(playedSong)
                _currentSong.value = playedSong
                // Cập nhật trạng thái Like ngay khi bắt đầu bài hát mới
                checkLikeStatus(playedSong.songId)
            }

            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                recentlyPlayedRepository.addPlayed(RecentlyPlayed(currentUser.uid, song.songId, System.currentTimeMillis()))
            }
            
            song.mainArtistId?.let { artistId ->
                if (artistId.isNotEmpty()) {
                    artistRepository.getArtistOnce(artistId)?.let { artist ->
                        _artistName.value = artist.name
                    } ?: run {
                        _artistName.value = song.artistName ?: ""
                    }
                } else {
                    _artistName.value = song.artistName ?: ""
                }
            } ?: run {
                _artistName.value = song.artistName ?: ""
            }
        }
    }
}