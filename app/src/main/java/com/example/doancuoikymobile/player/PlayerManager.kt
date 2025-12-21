package com.example.doancuoikymobile.player

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import android.content.Context

object PlayerManager {
    private var exoPlayer: ExoPlayer? = null
    private var currentSong: com.example.doancuoikymobile.model.Song? = null
    private val queue = mutableListOf<com.example.doancuoikymobile.model.Song>()

    fun init(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
    }

    fun getPlayer(): ExoPlayer = exoPlayer ?: throw IllegalStateException("Player not initialized")

    fun playSong(song: com.example.doancuoikymobile.model.Song) {
        currentSong = song
        val mediaItem = MediaItem.fromUri(song.audioUrl)
        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun play() = exoPlayer?.play()
    fun pause() = exoPlayer?.pause()
    fun seekTo(position: Long) = exoPlayer?.seekTo(position)
    fun getCurrentPosition() = exoPlayer?.currentPosition ?: 0L
    fun getDuration() = exoPlayer?.duration ?: 0L
    fun isPlaying() = exoPlayer?.isPlaying ?: false

    fun getCurrentSong() = currentSong

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}