package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.player.PlayerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            while (true) {
                _currentSong.value = PlayerManager.getCurrentSong()
                _isPlaying.value = PlayerManager.isPlaying()
                _progress.value = PlayerManager.getCurrentPosition()
                _duration.value = PlayerManager.getDuration()
                delay(1000)
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

    // SỬA LỖI TẠI ĐÂY: Đổi startPlaying thành playSong để khớp với PlayerManager
    fun playSong(song: Song) {
        PlayerManager.playSong(song) // Gọi đúng tên hàm trong PlayerManager
        _currentSong.value = song
    }
}