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
import com.example.doancuoikymobile.repository.PlaylistRepository
import com.example.doancuoikymobile.data.remote.firebase.PlaylistRemoteDataSource
import com.example.doancuoikymobile.data.remote.firebase.PlaylistSongDataSource
import com.example.doancuoikymobile.model.Playlist
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.media3.common.Player

class PlayerViewModel(
    private val artistRepository: ArtistRepository = ArtistRepository(ArtistRemoteDataSource()),
    private val songRepository: SongRepository = SongRepository(),
    private val recentlyPlayedRepository: RecentlyPlayedRepository = RecentlyPlayedRepository(RecentlyPlayedDataSource()),
    private val authRepository: AuthRepository = AuthRepository(),
    private val likedSongRepository: LikedSongRepository = LikedSongRepository(LikedSongRemoteDataSource(FirebaseFirestore.getInstance())),
    private val playlistRepository: PlaylistRepository = PlaylistRepository(
        PlaylistRemoteDataSource(FirebaseFirestore.getInstance()),
        PlaylistSongDataSource(FirebaseFirestore.getInstance())
    )
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

    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val userPlaylists = _userPlaylists.asStateFlow()

    init {
        loadUserPlaylists()
        observePlayerState()

        PlayerManager.player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    playNext() // Tự động chuyển bài khi kết thúc
                }
            }
        })
    }

    // Hàm load danh sách playlist để hiển thị trong Dialog
    private fun loadUserPlaylists() {
        val userId = authRepository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            playlistRepository.watchUserPlaylists(userId).collect {
                _userPlaylists.value = it
            }
        }
    }

    // Hàm thực hiện thêm bài hát hiện tại vào một playlist cụ thể
    fun addCurrentSongToPlaylist(playlistId: String) {
        val song = _currentSong.value ?: return

        viewModelScope.launch {
            if (song.isOnline) {
                songRepository.saveSong(song)
            }
            playlistRepository.addSongToPlaylist(playlistId, song.songId)
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            while (PlayerManager.player != null) {
                val songFromPlayer = PlayerManager.getCurrentSong()

                if (songFromPlayer != null &&
                    songFromPlayer.songId != _currentSong.value?.songId
                ) {
                    _currentSong.value = songFromPlayer
                    _artistName.value = songFromPlayer.artistName.orEmpty()
                    checkLikeStatus(songFromPlayer.songId)
                }

                _isPlaying.value = PlayerManager.isPlaying()
                _progress.value = PlayerManager.getCurrentPosition()
                _duration.value = PlayerManager.getDuration()

                delay(1000)
            }
        }
    }

    private fun checkLikeStatus(songId: String) {
        val userId = authRepository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isCurrentSongLiked.value =
                likedSongRepository.isLiked(userId, songId)
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
        if (PlayerManager.isPlaying()) PlayerManager.pause()
        else PlayerManager.play()
    }

    fun seekTo(position: Long) {
        PlayerManager.seekTo(position)
    }

    fun playNext() {
        if (playlist.isEmpty()) return

        when (_repeatMode.value) {
            1 -> { // REPEAT ONE
                _currentSong.value?.let { playSong(it) }
                return
            }
        }

        if (currentPlaylistIndex == -1) {
            currentPlaylistIndex =
                playlist.indexOfFirst { it.songId == _currentSong.value?.songId }
        }

        currentPlaylistIndex = if (_isShuffle.value && playlist.size > 1) {
            (playlist.indices).filter { it != currentPlaylistIndex }.random()
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

            if (playedSong.audioUrl.isEmpty() && playedSong.songId.isNotEmpty()) {
                songRepository.getSongById(playedSong.songId)?.let {
                    if (it.audioUrl.isNotEmpty()) playedSong = it
                }
            }

            val finalUrl = when {
                playedSong.audioUrl.isNotEmpty() -> playedSong.audioUrl
                !playedSong.previewUrl.isNullOrEmpty() -> playedSong.previewUrl
                else -> null
            } ?: return@launch

            val songToPlay = playedSong.copy(audioUrl = finalUrl)

            if (songToPlay.isOnline) {
                songRepository.saveSong(songToPlay)
            }

            PlayerManager.playSong(songToPlay)
            _currentSong.value = songToPlay
            checkLikeStatus(songToPlay.songId)

            authRepository.getCurrentUser()?.let { user ->
                recentlyPlayedRepository.addPlayed(
                    RecentlyPlayed(
                        user.uid,
                        songToPlay.songId,
                        System.currentTimeMillis()
                    )
                )
            }

            songToPlay.mainArtistId
                ?.takeIf { it.isNotEmpty() }
                ?.let { artistId ->
                    artistRepository.getArtistOnce(artistId)?.let {
                        _artistName.value = it.name
                    } ?: run {
                        _artistName.value = songToPlay.artistName.orEmpty()
                    }
                } ?: run {
                _artistName.value = songToPlay.artistName.orEmpty()
            }
        }
    }
}