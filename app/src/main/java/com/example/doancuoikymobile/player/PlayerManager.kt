package com.example.doancuoikymobile.player

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import android.content.Context
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.repository.RecentlyPlayedRepository
import com.example.doancuoikymobile.repository.SongRepository
import com.example.doancuoikymobile.data.remote.firebase.RecentlyPlayedDataSource
import com.example.doancuoikymobile.model.RecentlyPlayed
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PlayerManager {
    var player: ExoPlayer? = null
        private set

    private var currentSong: Song? = null
    private var context: Context? = null
    private var recentlyPlayedRepo: RecentlyPlayedRepository? = null
    private var songRepo: SongRepository? = null

    fun init(context: Context) {
        if (player == null) {
            this.context = context
            player = ExoPlayer.Builder(context).build().apply {
                playWhenReady = true
            }
            // Init repo để lưu bài phát gần đây
            recentlyPlayedRepo = RecentlyPlayedRepository(RecentlyPlayedDataSource())
            songRepo = SongRepository()
        }
    }

    fun playSong(song: Song) {
        if (player == null && context != null) init(context!!)

        currentSong = song

        // Ưu tiên audioUrl (full MP3), fallback previewUrl (30s preview)
        val urlToPlay = when {
            song.audioUrl.isNotEmpty() -> song.audioUrl
            !song.previewUrl.isNullOrEmpty() -> song.previewUrl
            else -> ""
        }

        if (urlToPlay.isEmpty()) {
            android.util.Log.e(
                "PlayerManager_ERROR",
                "❌ CRITICAL: No playable URL for song '${song.title}' (ID: ${song.songId})\n" +
                "  - audioUrl: '${song.audioUrl}'\n" +
                "  - previewUrl: '${song.previewUrl}'\n" +
                "  Action: Refresh from Deezer API before playing"
            )
            return
        }

        val mediaItem = MediaItem.fromUri(urlToPlay)
        player?.let { exoPlayer ->
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            android.util.Log.d(
                "PlayerManager",
                "✓ Playing: ${song.title} | Source: ${if (song.audioUrl.isNotEmpty()) "FULL" else "PREVIEW"}"
            )
        }

        // 🔥 Lưu vào Firebase Recently Played ngay khi phát
        saveToRecentlyPlayed(song)
    }

    fun play() = player?.play()
    fun pause() = player?.pause()
    fun isPlaying() = player?.isPlaying ?: false
    fun getCurrentSong() = currentSong
    fun getDuration() = player?.duration ?: 0L
    fun getCurrentPosition() = player?.currentPosition ?: 0L
    fun seekTo(position: Long) = player?.seekTo(position)

    /**
     * Lưu bài vào Firebase (songs + recently_played)
     */
    private fun saveToRecentlyPlayed(song: Song) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && recentlyPlayedRepo != null && songRepo != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 1. Chỉ lưu dữ liệu tĩnh (NO audioUrl, NO previewUrl)
                    val staticSongData = Song(
                        songId = song.songId,
                        title = song.title,
                        duration = song.duration,
                        audioUrl = "",  // ⬜ Trống - fill tay sau khi có file
                        previewUrl = null,  // ❌ Không lưu - nó hết hạn trong 24h
                        coverUrl = song.coverUrl,
                        mainArtistId = song.mainArtistId,
                        artistName = song.artistName,
                        isOnline = song.isOnline
                    )
                    songRepo?.saveSongIfNotExists(staticSongData)
                    
                    // 2. Lưu vào recently_played
                    val recentlyPlayed = RecentlyPlayed(
                        userId = userId,
                        songId = song.songId,
                        playedAt = System.currentTimeMillis()
                    )
                    recentlyPlayedRepo?.addPlayed(recentlyPlayed)
                    android.util.Log.d("PlayerManager", "✓ Saved '${song.title}' to Songs & Recently Played")
                } catch (e: Exception) {
                    android.util.Log.e("PlayerManager", "❌ Failed to save: ${e.message}")
                }
            }
        }
    }

    fun release() {
        player?.release()
        player = null
        currentSong = null
    }
}