package com.example.doancuoikymobile.player

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import android.content.Context
import com.example.doancuoikymobile.model.Song

object PlayerManager {
    private var exoPlayer: ExoPlayer? = null
    private var currentSong: Song? = null
    private var context: Context? = null

    private val playlist = mutableListOf<Song>()
    private var currentIndex = -1

    fun init(context: Context) {
        if (exoPlayer == null) {
            this.context = context
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                playWhenReady = true
            }
        }
    }

    fun getPlayer(): ExoPlayer = exoPlayer ?: throw IllegalStateException("Player not initialized")

    fun playSong(song: Song) {
        if (exoPlayer == null && context != null) {
            init(context!!)
        }
        
        currentSong = song

        val urlToPlay = when {
            song.audioUrl.isNotEmpty() -> song.audioUrl
            !song.previewUrl.isNullOrEmpty() -> song.previewUrl
            else -> ""
        }

        if (urlToPlay.isEmpty()) return

        val mediaItem = MediaItem.fromUri(urlToPlay)
        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

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