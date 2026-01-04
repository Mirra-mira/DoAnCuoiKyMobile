package com.example.doancuoikymobile.player

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import android.content.Context
import com.example.doancuoikymobile.model.Song

object PlayerManager {
    var player: ExoPlayer? = null
        private set

    private var currentSong: Song? = null
    private var context: Context? = null

    fun init(context: Context) {
        if (player == null) {
            this.context = context
            player = ExoPlayer.Builder(context).build().apply {
                playWhenReady = true
            }
        }
    }

    fun playSong(song: Song) {
        if (player == null && context != null) init(context!!)

        currentSong = song

        // Ưu tiên audioUrl (vì ViewModel của chúng ta đã gán link preview vào audioUrl trước khi gửi sang đây)
        val urlToPlay = when {
            song.audioUrl.isNotEmpty() -> song.audioUrl
            !song.previewUrl.isNullOrEmpty() -> song.previewUrl
            else -> ""
        }

        if (urlToPlay.isEmpty()) {
            android.util.Log.e("DEBUG_PLAYER", "LỖI: Bài '${song.title}' (ID: ${song.songId}) không có link nhạc!")
            return
        }

        val mediaItem = MediaItem.fromUri(urlToPlay)
        player?.let { exoPlayer ->
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            android.util.Log.d("PlayerManager", "Đang phát: ${song.title} với URL: $urlToPlay")
        }
    }

    fun play() = player?.play()
    fun pause() = player?.pause()
    fun isPlaying() = player?.isPlaying ?: false
    fun getCurrentSong() = currentSong
    fun getDuration() = player?.duration ?: 0L
    fun getCurrentPosition() = player?.currentPosition ?: 0L
    fun seekTo(position: Long) = player?.seekTo(position)

    fun release() {
        player?.release()
        player = null
        currentSong = null
    }
}