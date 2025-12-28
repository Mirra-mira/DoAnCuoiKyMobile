package com.example.doancuoikymobile.player

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import android.content.Context
import com.example.doancuoikymobile.model.Song

object PlayerManager {
    private var exoPlayer: ExoPlayer? = null
    private var currentSong: Song? = null

    // Quản lý danh sách phát
    private val playlist = mutableListOf<Song>()
    private var currentIndex = -1

    fun init(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                // Tự động chuyển bài khi kết thúc
                playWhenReady = true
            }
        }
    }

    fun getPlayer(): ExoPlayer = exoPlayer ?: throw IllegalStateException("Player not initialized")

    /**
     * Quyết định nguồn phát nhạc: Ưu tiên audioUrl, nếu rỗng dùng previewUrl
     */
    fun playSong(song: Song) {
        currentSong = song

        // LOGIC QUAN TRỌNG: Kiểm tra nguồn nhạc từ Deezer hay Firebase
        val urlToPlay = if (song.audioUrl.isNotEmpty()) {
            song.audioUrl
        } else {
            song.previewUrl ?: ""
        }

        if (urlToPlay.isEmpty()) return

        val mediaItem = MediaItem.fromUri(urlToPlay)
        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    // --- Điều khiển bổ sung ---
    fun play() = exoPlayer?.play()
    fun pause() = exoPlayer?.pause()
    fun stop() = exoPlayer?.stop()

    fun isPlaying() = exoPlayer?.isPlaying ?: false
    fun getCurrentSong() = currentSong

    fun getDuration() = exoPlayer?.duration ?: 0L
    fun getCurrentPosition() = exoPlayer?.currentPosition ?: 0L
    fun seekTo(position: Long) = exoPlayer?.seekTo(position)

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        currentSong = null
    }
}