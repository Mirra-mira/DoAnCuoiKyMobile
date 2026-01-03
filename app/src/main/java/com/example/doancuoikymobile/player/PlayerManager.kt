package com.example.doancuoikymobile.player

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import android.content.Context
import com.example.doancuoikymobile.model.Song

object PlayerManager {
    // Đổi từ private var thành public var để ViewModel có thể addListener
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

    // Cập nhật lại các hàm để dùng biến 'player' mới
    fun playSong(song: Song) {
        if (player == null && context != null) init(context!!)

        currentSong = song
        val urlToPlay = if (song.audioUrl.isNotEmpty()) song.audioUrl else song.previewUrl ?: ""

        if (urlToPlay.isEmpty()) return

        val mediaItem = MediaItem.fromUri(urlToPlay)
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
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