package com.example.doancuoikymobile.utils

import android.media.MediaPlayer
import com.example.doancuoikymobile.model.Song

/**
 * Extension functions for audio playback with priority system
 *
 * IMPORTANT AUDIO PRIORITY:
 * 1. Song.audioUrl (FULL MP3 - user uploaded or full track) - PRIMARY
 * 2. Song.previewUrl (Deezer 30s preview) - FALLBACK
 *
 * This ensures Deezer preview never overwrites user's full MP3.
 */

/**
 * Smart play function that automatically handles priority
 *
 * Priority logic:
 * - If audioUrl is not empty (full track available) → play full MP3
 * - Else if previewUrl is available → play Deezer 30s preview
 * - Else → return false (no audio available)
 *
 * @param song Song to play
 * @return true if playable, false if no audio available
 */
fun MediaPlayer.playSong(song: Song): Boolean {
    val audioUrl = when {
        // Priority 1: Full MP3 (user uploaded or full track)
        song.audioUrl.isNotEmpty() && song.audioUrl.startsWith("http") -> {
            Logger.d("PlayerExt", "Playing FULL track: ${song.title} (${song.audioUrl.take(50)}...)")
            song.audioUrl
        }
        // Priority 2: Deezer preview (fallback)
        song.previewUrl != null && song.previewUrl.startsWith("http") -> {
            Logger.d("PlayerExt", "Playing PREVIEW: ${song.title} (${song.previewUrl.take(50)}...)")
            song.previewUrl
        }
        // No audio available
        else -> {
            Logger.w("PlayerExt", "No audio available for ${song.title}")
            return false
        }
    }

    return try {
        this.setDataSource(audioUrl)
        this.prepareAsync()
        true
    } catch (e: Exception) {
        Logger.e("PlayerExt", e)
        false
    }
}

/**
 * Play Deezer preview (deprecated - use playSong instead)
 *
 * @deprecated Use playSong() instead for automatic priority handling
 */
@Deprecated(
    "Use playSong(song) for automatic priority (full → preview)",
    replaceWith = ReplaceWith("this.playSong(song)")
)
fun MediaPlayer.playDeezerPreview(song: Song): Boolean {
    Logger.d("PlayerExt", "playDeezerPreview() is deprecated, delegating to playSong()")
    return this.playSong(song)
}

/**
 * Check if song has playable audio (full or preview)
 *
 * @return true if either audioUrl or previewUrl is valid
 */
fun Song.hasAudio(): Boolean {
    val hasFullAudio = this.audioUrl.isNotEmpty() && this.audioUrl.startsWith("http")
    val hasPreview = this.previewUrl != null && this.previewUrl.startsWith("http")
    return hasFullAudio || hasPreview
}

/**
 * Check if song has preview (deprecated - use hasAudio instead)
 *
 * @deprecated Use hasAudio() to check both full and preview
 */
@Deprecated(
    "Use hasAudio() to check both audioUrl and previewUrl",
    replaceWith = ReplaceWith("this.hasAudio()")
)
fun Song.hasPreview(): Boolean {
    return this.hasAudio()
}

/**
 * Get playable audio URL with priority
 *
 * @return URL to play (full or preview), or null if no audio
 */
fun Song.getPlayableUrl(): String? {
    return when {
        // Priority 1: Full MP3
        this.audioUrl.isNotEmpty() && this.audioUrl.startsWith("http") -> {
            this.audioUrl
        }
        // Priority 2: Deezer preview
        this.previewUrl != null && this.previewUrl.startsWith("http") -> {
            this.previewUrl
        }
        // No audio
        else -> null
    }
}

/**
 * Check if song has full track audio (not preview)
 *
 * @return true if audioUrl is valid (full track)
 */
fun Song.isFullTrack(): Boolean {
    return this.audioUrl.isNotEmpty() && this.audioUrl.startsWith("http")
}

/**
 * Check if song has only preview (no full track)
 *
 * @return true if previewUrl is valid but audioUrl is empty
 */
fun Song.isPreviewOnly(): Boolean {
    return this.audioUrl.isEmpty() && this.previewUrl != null && this.previewUrl.startsWith("http")
}

/**
 * Get audio type description for UI
 *
 * @return User-friendly description of what audio is available
 */
fun Song.getAudioTypeDescription(): String {
    return when {
        isFullTrack() -> "🎵 Full Song"
        isPreviewOnly() -> "🎶 30s Preview"
        else -> "❌ No Audio"
    }
}

/**
 * Log audio information for debugging
 */
fun Song.debugAudio() {
    Logger.d("Song.Audio", "Title: $title")
    Logger.d("Song.Audio", "audioUrl: ${if (audioUrl.isEmpty()) "EMPTY" else audioUrl.take(50) + "..."}")
    Logger.d("Song.Audio", "previewUrl: ${previewUrl?.take(50) ?: "NULL"}...")
    Logger.d("Song.Audio", "Has Audio: ${hasAudio()}")
    Logger.d("Song.Audio", "Audio Type: ${getAudioTypeDescription()}")
    Logger.d("Song.Audio", "Will Play: ${if (isFullTrack()) "FULL TRACK" else "PREVIEW"}")
}
