package com.example.doancuoikymobile.player

import android.media.MediaPlayer
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.utils.Logger

/**
 * Extension to play audio with priority:
 * 1. Full MP3 (Song.audioUrl) if available
 * 2. Deezer preview (Song.previewUrl) as fallback
 *
 * Usage:
 * mediaPlayer.playSong(song)
 *
 * Notes:
 * - Tries audioUrl first (full track)
 * - Falls back to previewUrl (30s preview) if audioUrl empty
 * - No download or caching needed
 */
fun MediaPlayer.playSong(song: Song) {
    try {
        val urlToPlay = if (song.audioUrl.isNotEmpty()) {
            song.audioUrl
        } else {
            song.previewUrl ?: ""
        }

        if (urlToPlay.isEmpty()) {
            Logger.d("PlayerExt", "No audio available for: ${song.title}")
            return
        }

        // Reset player state
        reset()

        // Set data source
        setDataSource(urlToPlay)

        // Prepare asynchronously
        prepareAsync()

        val audioType = if (song.audioUrl.isNotEmpty()) "full" else "preview"
        Logger.d("PlayerExt", "Playing ($audioType): ${song.title}")
    } catch (ex: Exception) {
        Logger.e("PlayerExt", ex)
    }
}

/**
 * Legacy: Play Deezer preview directly (deprecated)
 * Use playSong() instead which handles both full and preview
 * 
 * @deprecated Use playSong(song) instead
 */
@Deprecated("Use playSong(song) instead", ReplaceWith("playSong(song)"))
fun MediaPlayer.playDeezerPreview(song: Song) {
    playSong(song)
}

/**
 * Check if song has playable audio available
 *
 * Returns true if either:
 * - audioUrl is available (full MP3)
 * - previewUrl is available (Deezer preview)
 *
 * @return true if song can be played
 */
fun Song.hasAudio(): Boolean {
    return (audioUrl.isNotEmpty() && audioUrl.startsWith("http")) ||
           (!previewUrl.isNullOrEmpty() && previewUrl.startsWith("http"))
}

/**
 * Legacy: Check if song has preview
 * @deprecated Use hasAudio() instead
 */
@Deprecated("Use hasAudio() instead", ReplaceWith("hasAudio()"))
fun Song.hasPreview(): Boolean {
    return hasAudio()
}

/**
 * Safely get duration in readable format
 *
 * Deezer preview is always ~30 seconds
 * Full track duration available from API
 */
fun Song.getDurationFormatted(): String {
    val minutes = duration / 60
    val seconds = duration % 60
    return String.format("%d:%02d", minutes, seconds)
}
